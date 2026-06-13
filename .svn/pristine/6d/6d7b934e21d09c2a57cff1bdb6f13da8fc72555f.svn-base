package com.micsig.tbook.tbookscope.middleware.command;

import android.util.Log;

import com.micsig.tbook.scope.ScopeBase;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.RefChannel;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.SCPIParam;
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage;
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage;
import com.micsig.tbook.ui.wavezone.IWave;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by liwb on 2018/1/17.
 */

public class Command_Cursor {
    private static final String TAG = "Command_Cursor";
    //     new SCPICommandStruct(":CURSor:HORizontal","SCPI_Cursor","Horizontal"),//打开或关闭水平光标功能
//            new SCPICommandStruct(":CURSor:HORizontal?","SCPI_Cursor","HorizontalQ"),//查询水平光标功能
//            new SCPICommandStruct(":CURSor:VERTical","SCPI_Cursor","Vertical"),//打开或关闭垂直光标功能
//            new SCPICommandStruct(":CURSor:VERTical?","SCPI_Cursor","VerticalQ"),//查询垂直光标功能
//            new SCPICommandStruct(":CURSor:CX1","SCPI_Cursor","Cx1"),//设置垂直光标A的位置
//            new SCPICommandStruct(":CURSor:PLUS:CXA","SCPI_Cursor","Plus_Cxa"),//设置垂直光标A的位置
//            new SCPICommandStruct(":CURSor:CX1?","SCPI_Cursor","Cx1Q"),//查询垂直光标A的位置
//            new SCPICommandStruct(":CURSor:CX2","SCPI_Cursor","Cx2"),//设置垂直光标B的位置
//            new SCPICommandStruct(":CURSor:PLUS:CXB","SCPI_Cursor","Plus_Cxb"),//设置垂直光标B的位置
//            new SCPICommandStruct(":CURSor:CX2?","SCPI_Cursor","Cx2Q"),//查询垂直光标B的位置
//            new SCPICommandStruct(":CURSor:CY1","SCPI_Cursor","CY1"),//设置水平光标A的位置
//            new SCPICommandStruct(":CURSor:PLUS:CYA","SCPI_Cursor","PLUS_CYA"),//设置水平光标A的位置
//            new SCPICommandStruct(":CURSor:CY1?","SCPI_Cursor","CY1Q"),//查询水平光标A的位置
//            new SCPICommandStruct(":CURSor:CY2","SCPI_Cursor","CY2"),//设置水平光标B的位置
//            new SCPICommandStruct(":CURSor:PLUS:CYB","SCPI_Cursor","PLUS_CYB"),//设置水平光标B的位置
//            new SCPICommandStruct(":CURSor:CY2?","SCPI_Cursor","CY2Q"),//查询水平光标B的位置
//            new SCPICommandStruct(":CURSor:X1Value?","SCPI_Cursor","X1ValueQ"),//查询垂直光标A的X值
//            new SCPICommandStruct(":CURSor:X2Value?","SCPI_Cursor","X2ValueQ"),//查询垂直光标B的X值
//            new SCPICommandStruct(":CURSor:Y1Value?","SCPI_Cursor","Y1ValueQ"),//查询水平光标A的Y值
//            new SCPICommandStruct(":CURSor:Y2Value?","SCPI_Cursor","Y2ValueQ"),//查询水平光标B的Y值
//            new SCPICommandStruct(":CURSor:XDELta?","SCPI_Cursor","XdeltaQ"),//查询垂直光标A和B之间的差值，单位与水平单位相同
//            new SCPICommandStruct(":CURSor:YDELta?","SCPI_Cursor","YdeltaQ"),//查询水平光标A和B之间的差值，单位与垂直单位相同
//            new SCPICommandStruct(":CURSor:RATio?","SCPI_Cursor","RatioQ"),//查询水平光标A和B之间的差值与垂直光标A和B之间的差值之间的比值
//            new SCPICommandStruct(":CURSor:SOURce","SCPI_Cursor","Source"),//设置光标测量的通道源
//            new SCPICommandStruct(":CURSor:SOURce?","SCPI_Cursor","SourceQ"),//查询光标测量的通道源
//            new SCPICommandStruct(":CURSor:FREQ?","SCPI_Cursor","FreqQ"),//查询垂直光标x1和x2之间的1/x，单位HZ


    private boolean XYMode_Horizontal = false;
    private boolean YTMode_Horizontal = false;
    private boolean XYMode_Vertical = false;
    private boolean YTMode_Vertical = false;

    private int XYCX1 = 0;
    private int YTCX1 = 0;
    private int XYCX2 = 0;
    private int YTCX2 = 0;

    private double XYCY1 = 0;
    private double YTCY1 = 0;
    private double XYCY2 = 0;
    private double YTCY2 = 0;

    //当前光标源
    private int source = 0;
    //region 测量数据
    private CursorMeasureInfo cursorMeasureInfo = new CursorMeasureInfo();

    public void setCursorMeasureInfo(double Y1,double Y2,double deltaY,double X1,double X2,double deltaX,double deltaX1,double S) {
        this.cursorMeasureInfo.Y1=Y1;
        this.cursorMeasureInfo.Y2=Y2;
        this.cursorMeasureInfo.deltaY=deltaY;
        this.cursorMeasureInfo.X1=X1;
        this.cursorMeasureInfo.X2=X2;
        this.cursorMeasureInfo.deltaX=deltaX;
        this.cursorMeasureInfo.deltaX1=deltaX1;
        this.cursorMeasureInfo.S=S;
    }

    class CursorMeasureInfo {
        double Y1;
        double Y2;
        double deltaY;
        double X1;
        double X2;
        double deltaX;
        double deltaX1;
        double S;
    }
    //endregion

    /**
     * 打开或关闭水平光标功能
     */
    public void Horizontal(boolean isOpen, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            if (YTMode_Horizontal == isOpen) return;
            YTMode_Horizontal = isOpen;
        } else {
//            if (XYMode_Horizontal == isOpen) return;
            XYMode_Horizontal = isOpen;
        }
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL);
            msgToUI.setParam(String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean HorizontalQ() {
        return CursorManage.getInstance().getRowVisible();
//        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            return YTMode_Horizontal;
//        } else {
//            return XYMode_Horizontal;
//        }
    }

    /**
     * 打开或关闭垂直光标功能
     */
    public void Vertical(boolean isOpen, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            if (YTMode_Vertical == isOpen) return;
            YTMode_Vertical = isOpen;
        } else {
//            if (XYMode_Vertical == isOpen) return;
            XYMode_Vertical = isOpen;
        }
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL);
            msgToUI.setParam(String.valueOf(isOpen));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public boolean VerticalQ() {
        return CursorManage.getInstance().getColVisible();
//        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            return YTMode_Vertical;
//        } else {
//            return XYMode_Vertical;
//        }
    }

    /**
     * 设置垂直光标A的位置
     */
    public void Cx1(int currX, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            if (YTCX1 == currX) return;
            YTCX1 = currX;
        } else {
            if (XYCX1 == currX) return;
            XYCX1 = currX;
        }
        if (isUpdateUI) {
            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, currX);
        }
    }

    public void Plus_Cxa(int plusX, boolean isUpdateUI) {
        int param = Cx1Q();
        param += plusX;
        Cx1(param, isUpdateUI);
    }

    public int Cx1Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            return YTCX1;
        } else {
            return XYCX1;
        }
    }

    /**
     * 设置垂直光标B的位置
     */
    public void Cx2(int currX, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            if (YTCX2 == currX) return;
            YTCX2 = currX;
        } else {
            if (XYCX2 == currX) return;
            XYCX2 = currX;
        }
        if (isUpdateUI) {
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, currX);
        }
    }

    public void Plus_Cxb(int plusX, boolean isUpdateUI) {
        int param = Cx2Q();
        param += plusX;
        Cx2(param, isUpdateUI);
    }

    public int Cx2Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            return YTCX2;
        } else {
            return XYCX2;
        }
    }

    /**
     * 设置水平光标A的位置
     */
    public void CY1(double currY, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            if (YTCY1 == currY) return;
            YTCY1 = currY;
        } else {
            if (XYCY1 == currY) return;
            XYCY1 = currY;
        }
        if (isUpdateUI) {
            CursorManage.getInstance().setCursor(TChan.Cursor_row_1, currY);
        }
    }

    public void PLUS_CYA(int plusY, boolean isUpdateUI) {
        double param = CY1Q();
        param += plusY;
        CY1(param, isUpdateUI);
    }

    public double CY1Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            return YTCY1;
        } else {
            return XYCY1;
        }
    }

    /**
     * 设置水平光标B的位置
     */
    public void CY2(double currY, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            if (YTCY2 == currY) return;
            YTCY2 = currY;
        } else {
            if (XYCY2 == currY) return;
            XYCY2 = currY;
        }
        if (isUpdateUI) {
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2, currY);
        }
    }

    public void PLUS_CYB(int plusY, boolean isUpdateUI) {
        double param = CY2Q();
        param += plusY;
        CY2(param, isUpdateUI);
    }

    public double CY2Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            return YTCY2;
        } else {
            return XYCY2;
        }
    }

    public void X1Value(double x1,boolean isUpdateUI) {

        int currX;
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            int chIdx = ChannelFactory.getChActivate();
            double timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
            long curTimeX = 0;
            if (ChannelFactory.isDynamicCh(chIdx)
                    || ChannelFactory.isMathCh(chIdx)
                    || ChannelFactory.isSerialCh(chIdx)) {
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) {
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT();
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();
                } else {
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
                }
            } else if (ChannelFactory.isRefCh(chIdx)) {
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
                timeEvery = refChannel.getRefTimePerPix();
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();
            }
            currX = (int) Math.round(x1 / timeEvery + curTimeX);

        }else{
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();
            double xx = WaveManage.get().getPositionY(TChan.Ch1);
            currX = (int) Math.round(x1 / chEveryX + xx) ;
        }

        Cx1(currX, isUpdateUI);
    }

    public void X2Value(double x2,boolean isUpdateUI) {
        int currX;
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            int chIdx = ChannelFactory.getChActivate();
            double timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
            long curTimeX = 0;
            if (ChannelFactory.isDynamicCh(chIdx)
                    || ChannelFactory.isMathCh(chIdx)
                    || ChannelFactory.isSerialCh(chIdx)) {
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) {
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT();
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels();
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView();
                } else {
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix();
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix();
                }
            } else if (ChannelFactory.isRefCh(chIdx)) {
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx);
                timeEvery = refChannel.getRefTimePerPix();
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix();
            }
            currX = (int) Math.round(x2 / timeEvery + curTimeX);
        }else{
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix();
            double xx = WaveManage.get().getPositionY(TChan.Ch1);
            currX = (int) Math.round(x2 / chEveryX + xx) ;
        }
        Cx2(currX, isUpdateUI);
    }

    public void Y1Value(double x1,boolean isUpdateUI) {

        double currX;
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            int chIdx = ChannelFactory.getChActivate();
            double chEvery = 0;

            double curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chIdx));

            if (ChannelFactory.isDynamicCh(chIdx)) {
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isMathCh(chIdx)) {
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isRefCh(chIdx)) {
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix();
            }

            currX = curChY - x1/chEvery;

        }else{
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();
            double xx = WaveManage.get().getPositionY(TChan.Ch2);
            currX = xx - x1 / chEveryX;
        }

        CY1(currX, isUpdateUI);
    }

    public void Y2Value(double x2,boolean isUpdateUI) {
        double currX;
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
            int chIdx = ChannelFactory.getChActivate();
            double chEvery = 0;

            double curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chIdx));

            if (ChannelFactory.isDynamicCh(chIdx)) {
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isMathCh(chIdx)) {
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix();
            } else if (ChannelFactory.isRefCh(chIdx)) {
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix();
            }

            currX = curChY - x2/chEvery;

        }else{

            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix();
            double xx = WaveManage.get().getPositionY(TChan.Ch2);
            currX = xx - x2 / chEveryX;
        }
        CY2(currX, isUpdateUI);
    }

    /**
     * 查询垂直光标A的X值
     */
    public double X1ValueQ() {
        return this.cursorMeasureInfo.X1;
    }

    public double X2ValueQ() {
        return this.cursorMeasureInfo.X2;
    }

    public double Y1ValueQ() {
        return this.cursorMeasureInfo.Y1;
    }

    public double Y2ValueQ() {
        return this.cursorMeasureInfo.Y2;
    }

    public double XdeltaQ() {
        return this.cursorMeasureInfo.deltaX;
    }

    public double YdeltaQ() {
        return this.cursorMeasureInfo.deltaY;
    }

    public double RatioQ() {
        return this.cursorMeasureInfo.S;
    }

    /**
     * 设置光标测量的通道源
     */
    public void Source(int chIndex, boolean isUpdateUI) {
        if (chIndex == source) return;
        this.source = chIndex;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_SOURCE);
            msgToUI.setParam(String.valueOf(chIndex));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    public int SourceQ() {
        return this.source;
    }

    /**
     * 查询垂直光标x1和x2之间的1/x，单位HZ
     */
    public double FreqQ() {
        return this.cursorMeasureInfo.deltaX1;
    }

    public void Trace(boolean enable,boolean isUpdateUI){
        CursorManage.getInstance().setEnableCursorTrance(enable);
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_SETTING_TRACE);
            msgToUI.setParam(String.valueOf(enable));
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public boolean TraceQ(){
        return CursorManage.getInstance().isEnableCursorTrance();
    }
}
