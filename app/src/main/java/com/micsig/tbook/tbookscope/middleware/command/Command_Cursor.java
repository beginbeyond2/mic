package com.micsig.tbook.tbookscope.middleware.command; // 命令子包，SCPI光标命令处理

import android.util.Log; // Android日志工具

import com.micsig.tbook.scope.ScopeBase; // 示波器基础配置
import com.micsig.tbook.scope.channel.ChannelFactory; // 通道工厂
import com.micsig.tbook.scope.channel.RefChannel; // 参考通道
import com.micsig.tbook.scope.horizontal.HorizontalAxis; // 水平轴
import com.micsig.tbook.scope.horizontal.HorizontalAxisMath; // 数学通道水平轴
import com.micsig.tbook.tbookscope.rxjava.RxBus; // RxJava事件总线
import com.micsig.tbook.tbookscope.rxjava.RxEnum; // RxJava事件枚举
import com.micsig.tbook.tbookscope.scpi.SCPIParam; // SCPI参数
import com.micsig.tbook.tbookscope.wavezone.display.CursorManage; // 光标管理
import com.micsig.tbook.tbookscope.wavezone.wave.WaveManage; // 波形管理
import com.micsig.tbook.ui.wavezone.IWave; // 波形接口
import com.micsig.tbook.ui.wavezone.TChan; // 通道号定义

/**
 * Created by liwb on 2018/1/17.
 */

/*
 * +-----------------------------------------------------------------------------+
 * |                            Command_Cursor                                    |
 * +-----------------------------------------------------------------------------+
 * | 模块定位：SCPI命令中间件 - 光标(Cursor)命令处理层                           |
 * | 核心职责：解析并执行示波器光标相关的SCPI指令，管理水平和垂直光标的位置、      |
 * |          开关状态，以及光标测量数据（X/Y值、差值、频率、比值等）             |
 * | 架构设计：属于Command子模块，由Command单例统一调度；                         |
 * |          根据当前时基模式(YT/XY)分别存储光标状态；                          |
 * |          光标位置以像素为单位存储，与物理量的转换在XValue/YValue方法中完成    |
 * | 数据流向：SCPI指令 → Command_Cursor → 光标状态(YT/XY分存)                  |
 * |                            → CursorManage(光标管理器)                       |
 * |                            → CommandMsgToUI → RxBus → UI层                  |
 * | 依赖关系：Command(单例入口)、Command_Display(时基模式)、CursorManage、       |
 * |           ChannelFactory(通道工厂)、HorizontalAxis(水平轴)、WaveManage       |
 * | 使用场景：远程SCPI控制中设置/查询光标位置、光标开关、光标测量值             |
 * +-----------------------------------------------------------------------------+
 */
public class Command_Cursor {
    private static final String TAG = "Command_Cursor"; // 日志标签
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


    private boolean XYMode_Horizontal = false; // XY模式下水平光标开关
    private boolean YTMode_Horizontal = false; // YT模式下水平光标开关
    private boolean XYMode_Vertical = false; // XY模式下垂直光标开关
    private boolean YTMode_Vertical = false; // YT模式下垂直光标开关

    private int XYCX1 = 0; // XY模式下垂直光标A的像素位置
    private int YTCX1 = 0; // YT模式下垂直光标A的像素位置
    private int XYCX2 = 0; // XY模式下垂直光标B的像素位置
    private int YTCX2 = 0; // YT模式下垂直光标B的像素位置

    private double XYCY1 = 0; // XY模式下水平光标A的像素位置
    private double YTCY1 = 0; // YT模式下水平光标A的像素位置
    private double XYCY2 = 0; // XY模式下水平光标B的像素位置
    private double YTCY2 = 0; // YT模式下水平光标B的像素位置

    //当前光标源
    private int source = 0; // 光标测量的通道源索引
    //region 测量数据
    private CursorMeasureInfo cursorMeasureInfo = new CursorMeasureInfo(); // 光标测量数据对象

    /**
     * 设置光标测量数据
     * @param Y1 水平光标A的Y值
     * @param Y2 水平光标B的Y值
     * @param deltaY 水平光标Y差值
     * @param X1 垂直光标A的X值
     * @param X2 垂直光标B的X值
     * @param deltaX 垂直光标X差值
     * @param deltaX1 垂直光标X差值的倒数（频率）
     * @param S 光标比值
     */
    public void setCursorMeasureInfo(double Y1,double Y2,double deltaY,double X1,double X2,double deltaX,double deltaX1,double S) {
        this.cursorMeasureInfo.Y1=Y1; // 设置Y1值
        this.cursorMeasureInfo.Y2=Y2; // 设置Y2值
        this.cursorMeasureInfo.deltaY=deltaY; // 设置Y差值
        this.cursorMeasureInfo.X1=X1; // 设置X1值
        this.cursorMeasureInfo.X2=X2; // 设置X2值
        this.cursorMeasureInfo.deltaX=deltaX; // 设置X差值
        this.cursorMeasureInfo.deltaX1=deltaX1; // 设置X差值倒数（频率）
        this.cursorMeasureInfo.S=S; // 设置比值
    }

    /**
     * 光标测量信息内部类，存储光标的各项测量值
     */
    class CursorMeasureInfo {
        double Y1; // 水平光标A的Y值
        double Y2; // 水平光标B的Y值
        double deltaY; // 水平光标Y差值
        double X1; // 垂直光标A的X值
        double X2; // 垂直光标B的X值
        double deltaX; // 垂直光标X差值
        double deltaX1; // X差值倒数（频率）
        double S; // 光标比值
    }
    //endregion

    /**
     * 打开或关闭水平光标功能
     * 对应SCPI指令: :CURSor:HORizontal
     * @param isOpen 是否打开水平光标
     * @param isUpdateUI 是否通知UI更新
     */
    public void Horizontal(boolean isOpen, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
//            if (YTMode_Horizontal == isOpen) return;
            YTMode_Horizontal = isOpen; // 更新YT模式下水平光标状态
        } else { // XY模式
//            if (XYMode_Horizontal == isOpen) return;
            XYMode_Horizontal = isOpen; // 更新XY模式下水平光标状态
        }
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_HORIZONTAL); // 设置消息标志为水平光标
            msgToUI.setParam(String.valueOf(isOpen)); // 设置开关参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询水平光标是否打开
     * 对应SCPI指令: :CURSor:HORizontal?
     * @return 水平光标是否打开
     */
    public boolean HorizontalQ() {
        return CursorManage.getInstance().getRowVisible(); // 直接从光标管理器获取行光标可见性
//        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            return YTMode_Horizontal;
//        } else {
//            return XYMode_Horizontal;
//        }
    }

    /**
     * 打开或关闭垂直光标功能
     * 对应SCPI指令: :CURSor:VERTical
     * @param isOpen 是否打开垂直光标
     * @param isUpdateUI 是否通知UI更新
     */
    public void Vertical(boolean isOpen, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
//            if (YTMode_Vertical == isOpen) return;
            YTMode_Vertical = isOpen; // 更新YT模式下垂直光标状态
        } else { // XY模式
//            if (XYMode_Vertical == isOpen) return;
            XYMode_Vertical = isOpen; // 更新XY模式下垂直光标状态
        }
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_VERTICAL); // 设置消息标志为垂直光标
            msgToUI.setParam(String.valueOf(isOpen)); // 设置开关参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询垂直光标是否打开
     * 对应SCPI指令: :CURSor:VERTical?
     * @return 垂直光标是否打开
     */
    public boolean VerticalQ() {
        return CursorManage.getInstance().getColVisible(); // 直接从光标管理器获取列光标可见性
//        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) {
//            return YTMode_Vertical;
//        } else {
//            return XYMode_Vertical;
//        }
    }

    /**
     * 设置垂直光标A的位置（像素）
     * 对应SCPI指令: :CURSor:CX1
     * @param currX 垂直光标A的像素位置
     * @param isUpdateUI 是否通知UI更新
     */
    public void Cx1(int currX, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            if (YTCX1 == currX) return; // 位置未变则直接返回
            YTCX1 = currX; // 更新YT模式垂直光标A位置
        } else { // XY模式
            if (XYCX1 == currX) return; // 位置未变则直接返回
            XYCX1 = currX; // 更新XY模式垂直光标A位置
        }
        if (isUpdateUI) { // 需要通知UI更新
            CursorManage.getInstance().setCursor(TChan.Cursor_col_1, currX); // 设置光标管理器中垂直光标A的位置
        }
    }

    /**
     * 垂直光标A位置递增（Plus指令）
     * 对应SCPI指令: :CURSor:PLUS:CXA
     * @param plusX 递增量（像素）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Cxa(int plusX, boolean isUpdateUI) {
        int param = Cx1Q(); // 获取当前垂直光标A位置
        param += plusX; // 加上递增量
        Cx1(param, isUpdateUI); // 设置新的位置
    }

    /**
     * 查询垂直光标A的位置
     * 对应SCPI指令: :CURSor:CX1?
     * @return 垂直光标A的像素位置
     */
    public int Cx1Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            return YTCX1; // 返回YT模式垂直光标A位置
        } else { // XY模式
            return XYCX1; // 返回XY模式垂直光标A位置
        }
    }

    /**
     * 设置垂直光标B的位置（像素）
     * 对应SCPI指令: :CURSor:CX2
     * @param currX 垂直光标B的像素位置
     * @param isUpdateUI 是否通知UI更新
     */
    public void Cx2(int currX, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            if (YTCX2 == currX) return; // 位置未变则直接返回
            YTCX2 = currX; // 更新YT模式垂直光标B位置
        } else { // XY模式
            if (XYCX2 == currX) return; // 位置未变则直接返回
            XYCX2 = currX; // 更新XY模式垂直光标B位置
        }
        if (isUpdateUI) { // 需要通知UI更新
            CursorManage.getInstance().setCursor(TChan.Cursor_col_2, currX); // 设置光标管理器中垂直光标B的位置
        }
    }

    /**
     * 垂直光标B位置递增（Plus指令）
     * 对应SCPI指令: :CURSor:PLUS:CXB
     * @param plusX 递增量（像素）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Plus_Cxb(int plusX, boolean isUpdateUI) {
        int param = Cx2Q(); // 获取当前垂直光标B位置
        param += plusX; // 加上递增量
        Cx2(param, isUpdateUI); // 设置新的位置
    }

    /**
     * 查询垂直光标B的位置
     * 对应SCPI指令: :CURSor:CX2?
     * @return 垂直光标B的像素位置
     */
    public int Cx2Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            return YTCX2; // 返回YT模式垂直光标B位置
        } else { // XY模式
            return XYCX2; // 返回XY模式垂直光标B位置
        }
    }

    /**
     * 设置水平光标A的位置（像素）
     * 对应SCPI指令: :CURSor:CY1
     * @param currY 水平光标A的像素位置
     * @param isUpdateUI 是否通知UI更新
     */
    public void CY1(double currY, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            if (YTCY1 == currY) return; // 位置未变则直接返回
            YTCY1 = currY; // 更新YT模式水平光标A位置
        } else { // XY模式
            if (XYCY1 == currY) return; // 位置未变则直接返回
            XYCY1 = currY; // 更新XY模式水平光标A位置
        }
        if (isUpdateUI) { // 需要通知UI更新
            CursorManage.getInstance().setCursor(TChan.Cursor_row_1, currY); // 设置光标管理器中水平光标A的位置
        }
    }

    /**
     * 水平光标A位置递增（Plus指令）
     * 对应SCPI指令: :CURSor:PLUS:CYA
     * @param plusY 递增量
     * @param isUpdateUI 是否通知UI更新
     */
    public void PLUS_CYA(int plusY, boolean isUpdateUI) {
        double param = CY1Q(); // 获取当前水平光标A位置
        param += plusY; // 加上递增量
        CY1(param, isUpdateUI); // 设置新的位置
    }

    /**
     * 查询水平光标A的位置
     * 对应SCPI指令: :CURSor:CY1?
     * @return 水平光标A的像素位置
     */
    public double CY1Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            return YTCY1; // 返回YT模式水平光标A位置
        } else { // XY模式
            return XYCY1; // 返回XY模式水平光标A位置
        }
    }

    /**
     * 设置水平光标B的位置（像素）
     * 对应SCPI指令: :CURSor:CY2
     * @param currY 水平光标B的像素位置
     * @param isUpdateUI 是否通知UI更新
     */
    public void CY2(double currY, boolean isUpdateUI) {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            if (YTCY2 == currY) return; // 位置未变则直接返回
            YTCY2 = currY; // 更新YT模式水平光标B位置
        } else { // XY模式
            if (XYCY2 == currY) return; // 位置未变则直接返回
            XYCY2 = currY; // 更新XY模式水平光标B位置
        }
        if (isUpdateUI) { // 需要通知UI更新
            CursorManage.getInstance().setCursor(TChan.Cursor_row_2, currY); // 设置光标管理器中水平光标B的位置
        }
    }

    /**
     * 水平光标B位置递增（Plus指令）
     * 对应SCPI指令: :CURSor:PLUS:CYB
     * @param plusY 递增量
     * @param isUpdateUI 是否通知UI更新
     */
    public void PLUS_CYB(int plusY, boolean isUpdateUI) {
        double param = CY2Q(); // 获取当前水平光标B位置
        param += plusY; // 加上递增量
        CY2(param, isUpdateUI); // 设置新的位置
    }

    /**
     * 查询水平光标B的位置
     * 对应SCPI指令: :CURSor:CY2?
     * @return 水平光标B的像素位置
     */
    public double CY2Q() {
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            return YTCY2; // 返回YT模式水平光标B位置
        } else { // XY模式
            return XYCY2; // 返回XY模式水平光标B位置
        }
    }

    /**
     * 设置垂直光标A的X值（物理量），自动转换为像素位置
     * 对应SCPI指令: :CURSor:X1Value
     * @param x1 垂直光标A的X物理量值（YT模式为时间，XY模式为电压）
     * @param isUpdateUI 是否通知UI更新
     */
    public void X1Value(double x1,boolean isUpdateUI) {

        int currX; // 转换后的像素位置
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            int chIdx = ChannelFactory.getChActivate(); // 获取当前活动通道索引
            double timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 获取每像素对应的时间值
            long curTimeX = 0; // 屏幕中心对应的参考时间位置（像素）
            if (ChannelFactory.isDynamicCh(chIdx) // 动态通道
                    || ChannelFactory.isMathCh(chIdx) // 数学通道
                    || ChannelFactory.isSerialCh(chIdx)) { // 串行通道
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) { // FFT数学通道
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT(); // 获取FFT水平轴
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels(); // 计算FFT每像素频率值
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView(); // 计算FFT屏幕中心位置
                } else { // 非FFT通道
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 使用普通水平轴的每像素时间值
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 计算屏幕中心位置
                }
            } else if (ChannelFactory.isRefCh(chIdx)) { // 参考通道
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx); // 获取参考通道对象
                timeEvery = refChannel.getRefTimePerPix(); // 使用参考通道的每像素时间值
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix(); // 计算屏幕中心位置
            }
            currX = (int) Math.round(x1 / timeEvery + curTimeX); // 物理量转像素：x1/每像素值 + 屏幕中心偏移

        }else{ // XY模式
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix(); // CH1每像素电压值
            double xx = WaveManage.get().getPositionY(TChan.Ch1); // CH1的垂直位置
            currX = (int) Math.round(x1 / chEveryX + xx) ; // X物理量转像素
        }

        Cx1(currX, isUpdateUI); // 设置垂直光标A的像素位置
    }

    /**
     * 设置垂直光标B的X值（物理量），自动转换为像素位置
     * 对应SCPI指令: :CURSor:X2Value
     * @param x2 垂直光标B的X物理量值
     * @param isUpdateUI 是否通知UI更新
     */
    public void X2Value(double x2,boolean isUpdateUI) {
        int currX; // 转换后的像素位置
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            int chIdx = ChannelFactory.getChActivate(); // 获取当前活动通道索引
            double timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 获取每像素对应的时间值
            long curTimeX = 0; // 屏幕中心对应的参考时间位置（像素）
            if (ChannelFactory.isDynamicCh(chIdx) // 动态通道
                    || ChannelFactory.isMathCh(chIdx) // 数学通道
                    || ChannelFactory.isSerialCh(chIdx)) { // 串行通道
                if (ChannelFactory.isMath_FFT_Ch(chIdx)) { // FFT数学通道
                    HorizontalAxisMath horizontalAxisMath = ChannelFactory.getMathChannel(chIdx).getHorizontalAxisMathFFT(); // 获取FFT水平轴
                    timeEvery = horizontalAxisMath.fftXScaleIdVal() / ScopeBase.getHorizonPerGridPixels(); // 计算FFT每像素频率值
                    curTimeX = ScopeBase.getWidth() / 2 - horizontalAxisMath.getXPosOfView(); // 计算FFT屏幕中心位置
                } else { // 非FFT通道
                    timeEvery = HorizontalAxis.getInstance().getTimesPrePix(); // 使用普通水平轴的每像素时间值
                    curTimeX = ScopeBase.getWidth() / 2 - HorizontalAxis.getInstance().getTimePoseOfViewPix(); // 计算屏幕中心位置
                }
            } else if (ChannelFactory.isRefCh(chIdx)) { // 参考通道
                RefChannel refChannel = ChannelFactory.getRefChannel(chIdx); // 获取参考通道对象
                timeEvery = refChannel.getRefTimePerPix(); // 使用参考通道的每像素时间值
                curTimeX = ScopeBase.getWidth() / 2 - refChannel.getTimePoseOfViewPix(); // 计算屏幕中心位置
            }
            currX = (int) Math.round(x2 / timeEvery + curTimeX); // 物理量转像素
        }else{ // XY模式
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH1).getVerticalPerPix(); // CH1每像素电压值
            double xx = WaveManage.get().getPositionY(TChan.Ch1); // CH1的垂直位置
            currX = (int) Math.round(x2 / chEveryX + xx) ; // X物理量转像素
        }
        Cx2(currX, isUpdateUI); // 设置垂直光标B的像素位置
    }

    /**
     * 设置水平光标A的Y值（物理量），自动转换为像素位置
     * 对应SCPI指令: :CURSor:Y1Value
     * @param x1 水平光标A的Y物理量值（电压）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Y1Value(double x1,boolean isUpdateUI) {

        double currX; // 转换后的像素位置
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            int chIdx = ChannelFactory.getChActivate(); // 获取当前活动通道索引
            double chEvery = 0; // 每像素对应的电压值

            double curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chIdx)); // 获取当前通道的UI垂直位置

            if (ChannelFactory.isDynamicCh(chIdx)) { // 动态通道
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix(); // 获取每像素电压值
            } else if (ChannelFactory.isMathCh(chIdx)) { // 数学通道
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix(); // 获取数学通道每像素电压值
            } else if (ChannelFactory.isRefCh(chIdx)) { // 参考通道
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix(); // 获取参考通道每像素电压值
            }

            currX = curChY - x1/chEvery; // Y物理量转像素：通道位置 - 物理值/每像素电压

        }else{ // XY模式
            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix(); // CH2每像素电压值
            double xx = WaveManage.get().getPositionY(TChan.Ch2); // CH2的垂直位置
            currX = xx - x1 / chEveryX; // Y物理量转像素
        }

        CY1(currX, isUpdateUI); // 设置水平光标A的像素位置
    }

    /**
     * 设置水平光标B的Y值（物理量），自动转换为像素位置
     * 对应SCPI指令: :CURSor:Y2Value
     * @param x2 水平光标B的Y物理量值（电压）
     * @param isUpdateUI 是否通知UI更新
     */
    public void Y2Value(double x2,boolean isUpdateUI) {
        double currX; // 转换后的像素位置
        if (Command.get().getDisplay().getRoutineTimeBaseModeQ() == Command_Display.RoutineTimeBaseMode_YT) { // YT模式
            int chIdx = ChannelFactory.getChActivate(); // 获取当前活动通道索引
            double chEvery = 0; // 每像素对应的电压值

            double curChY = WaveManage.get().getUIPositionY(TChan.toUiChNo(chIdx)); // 获取当前通道的UI垂直位置

            if (ChannelFactory.isDynamicCh(chIdx)) { // 动态通道
                chEvery = ChannelFactory.getDynamicChannel(chIdx).getVerticalPerPix(); // 获取每像素电压值
            } else if (ChannelFactory.isMathCh(chIdx)) { // 数学通道
                chEvery = ChannelFactory.getMathChannel(chIdx).getVerticalPerPix(); // 获取数学通道每像素电压值
            } else if (ChannelFactory.isRefCh(chIdx)) { // 参考通道
                chEvery = ChannelFactory.getRefChannel(chIdx).getVerticalPerPix(); // 获取参考通道每像素电压值
            }

            currX = curChY - x2/chEvery; // Y物理量转像素：通道位置 - 物理值/每像素电压

        }else{ // XY模式

            double chEveryX = ChannelFactory.getDynamicChannel(ChannelFactory.CH2).getVerticalPerPix(); // CH2每像素电压值
            double xx = WaveManage.get().getPositionY(TChan.Ch2); // CH2的垂直位置
            currX = xx - x2 / chEveryX; // Y物理量转像素
        }
        CY2(currX, isUpdateUI); // 设置水平光标B的像素位置
    }

    /**
     * 查询垂直光标A的X值
     * 对应SCPI指令: :CURSor:X1Value?
     * @return 垂直光标A的X值（物理量）
     */
    public double X1ValueQ() {
        return this.cursorMeasureInfo.X1; // 返回测量的X1值
    }

    /**
     * 查询垂直光标B的X值
     * 对应SCPI指令: :CURSor:X2Value?
     * @return 垂直光标B的X值（物理量）
     */
    public double X2ValueQ() {
        return this.cursorMeasureInfo.X2; // 返回测量的X2值
    }

    /**
     * 查询水平光标A的Y值
     * 对应SCPI指令: :CURSor:Y1Value?
     * @return 水平光标A的Y值（物理量）
     */
    public double Y1ValueQ() {
        return this.cursorMeasureInfo.Y1; // 返回测量的Y1值
    }

    /**
     * 查询水平光标B的Y值
     * 对应SCPI指令: :CURSor:Y2Value?
     * @return 水平光标B的Y值（物理量）
     */
    public double Y2ValueQ() {
        return this.cursorMeasureInfo.Y2; // 返回测量的Y2值
    }

    /**
     * 查询垂直光标A和B之间的X差值
     * 对应SCPI指令: :CURSor:XDELta?
     * @return X差值（物理量）
     */
    public double XdeltaQ() {
        return this.cursorMeasureInfo.deltaX; // 返回X差值
    }

    /**
     * 查询水平光标A和B之间的Y差值
     * 对应SCPI指令: :CURSor:YDELta?
     * @return Y差值（物理量）
     */
    public double YdeltaQ() {
        return this.cursorMeasureInfo.deltaY; // 返回Y差值
    }

    /**
     * 查询光标比值
     * 对应SCPI指令: :CURSor:RATio?
     * @return 光标比值
     */
    public double RatioQ() {
        return this.cursorMeasureInfo.S; // 返回比值
    }

    /**
     * 设置光标测量的通道源
     * 对应SCPI指令: :CURSor:SOURce
     * @param chIndex 通道源索引
     * @param isUpdateUI 是否通知UI更新
     */
    public void Source(int chIndex, boolean isUpdateUI) {
        if (chIndex == source) return; // 通道源未变则直接返回
        this.source = chIndex; // 更新光标通道源
        if (isUpdateUI) { // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_SOURCE); // 设置消息标志为光标源
            msgToUI.setParam(String.valueOf(chIndex)); // 设置通道源参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询光标测量的通道源
     * 对应SCPI指令: :CURSor:SOURce?
     * @return 通道源索引
     */
    public int SourceQ() {
        return this.source; // 返回当前光标通道源
    }

    /**
     * 查询垂直光标x1和x2之间的1/x，单位HZ
     * 对应SCPI指令: :CURSor:FREQ?
     * @return 频率值（Hz）
     */
    public double FreqQ() {
        return this.cursorMeasureInfo.deltaX1; // 返回X差值的倒数（频率）
    }

    /**
     * 设置光标跟踪模式
     * 对应SCPI指令: :CURSor:TRACE
     * @param enable 是否启用光标跟踪
     * @param isUpdateUI 是否通知UI更新
     */
    public void Trace(boolean enable,boolean isUpdateUI){
        CursorManage.getInstance().setEnableCursorTrance(enable); // 设置光标管理器的跟踪模式
        if (isUpdateUI){ // 需要通知UI更新
            CommandMsgToUI msgToUI = Command.get().getMsgToUI(); // 获取UI消息对象
            msgToUI.setFlag(CommandMsgToUI.FLAG_CURSOR_SETTING_TRACE); // 设置消息标志为光标跟踪
            msgToUI.setParam(String.valueOf(enable)); // 设置开关参数
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI); // 发送UI更新事件
        }
    }

    /**
     * 查询光标跟踪模式状态
     * 对应SCPI指令: :CURSor:TRACE?
     * @return 是否启用光标跟踪
     */
    public boolean TraceQ(){
        return CursorManage.getInstance().isEnableCursorTrance(); // 从光标管理器获取跟踪状态
    }
}
