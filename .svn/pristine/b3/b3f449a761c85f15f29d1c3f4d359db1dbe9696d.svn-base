package com.micsig.tbook.tbookscope.middleware.command;


import android.util.Log;

import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.scope.math.MathExprError;
import com.micsig.tbook.scope.math.MathExprWave;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.main.mainbottom.MainHolderBottom;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardformula.KeyBoardFormulaUtil;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

import java.text.DecimalFormat;

/**
 * Created by liwb on 2018/1/17.
 */

public class Command_Math_Advanced {
//new SCPICommandStruct(":MATH:ADVanced:EXPRession", "SCPI_Math_Advanced","Expression"),
//            new SCPICommandStruct(":MATH:ADVanced:EXPRession?","SCPI_Math_Advanced","ExpressionQ"),
//            new SCPICommandStruct(":MATH:ADVanced:VAR1","SCPI_Math_Advanced","Var1"),//设置高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR1?","SCPI_Math_Advanced","Var1Q"),//查询高级运算表达式中的变量1
//            new SCPICommandStruct(":MATH:ADVanced:VAR2","SCPI_Math_Advanced","Var2"),//设置高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:VAR2?","SCPI_Math_Advanced","Var2Q"),//查询高级运算表达式中的变量2
//            new SCPICommandStruct(":MATH:ADVanced:EXTent","SCPI_Math_Advanced","Extent"),//设置高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:EXTent?","SCPI_Math_Advanced","ExtentQ"),//查询高级运算结果的垂直档位
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet","SCPI_Math_Advanced","Offset"),//设置高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:OFFSet?","SCPI_Math_Advanced","OffsetQ"),//查询高级运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:ADVanced:UNIT", "SCPI_Math_Advanced","Unit"),
//            new SCPICommandStruct(":MATH:ADVanced:UNIT?", "SCPI_Math_Advanced","UnitQ"),


    private String express="";
    private double var1;
    private double var2;
    private double extent;
    private double offset;
    private String unit;
    private double minVar=-9.9999E+9;
    private double maxVar=9.9999E+9;

    private DecimalFormat decimalFormat = new DecimalFormat("0.0000E0");
    /** 设置高级运算的表达式 */
    public void Expression(int mathIndex, String express, boolean isUpdateUI) {
//        if (this.express.equals(express)) return;
        express = express.replace("CH","ch");
        express=express.replace("*","×").replace("/","÷").replace("pi","π");
        MathExprError mathExprError = MathExprWave.isExprValid(
                KeyBoardFormulaUtil.amFormulaToScope(express.trim(), MainHolderBottom.getCenterTimeBase()));
        if (!mathExprError.isSuccess() || express.length()>37) return;

        this.express=express.trim();
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Express);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(express.trim());
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 查询高级运算的表达式 */
    public  String ExpressionQ(){return express;}


    private boolean isCorrect(String s){
        if(s.contains("E")) {
            String[] param = s.split("E");

            if (param[1].replace("+", "").replace("-", "").length() > 1) {
                return false;
            }
        }
        return true;
    }

    /** 设置高级运算表达式中的变量1 */
    public void Var1(int mathIndex, double var1, boolean isUpdateUI) {
        if (var1<minVar) var1=minVar;
        if (var1>maxVar) var1=maxVar;
        String s= decimalFormat.format(var1);
        Log.d("zhuzh","s:" + s + "var1:" + var1);
        if (isCorrect(s)==false) return;
        this.var1=var1;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Var1);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(s);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 查询高级运算表达式中的变量1 */
    public  double Var1Q(){return var1;}

    /** 设置高级运算表达式中的变量2 */
    public  void Var2(int mathIndex, double var2,boolean isUpdateUI){
        if (var2<minVar) var2=minVar;
        if (var2>maxVar) var2=maxVar;
        String s= decimalFormat.format(var2);
        if (isCorrect(s)==false) return;
        this.var2=var2;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Var2);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(s);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 查询高级运算表达式中的变量2 */
    public  double Var2Q(){return var2;}

    /** 设置高级运算结果的垂直档位 */
    public void Extent(int mathIndex, double extent, boolean isUpdateUI)
    {
//        if (this.extent==extent) return;
        this.extent=extent;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Extent);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 查询高级运算结果的垂直档位 */
    public  double ExtentQ(){return extent;}

    /** 设置高级运算结果的垂直偏移 */
    public  void Offset(int mathIndex, double offset,boolean isUpdateUI){
//        if (this.offset==offset) return;
        this.offset=offset;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Offset);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(offset);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }

    /** 查询高级运算结果的垂直偏移 */
    public String OffsetQ(int mathIndex) {
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex);
        double pos;
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//        pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
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
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION + TChan.toUiChNo(mathIndex));
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos;
        String unit = ChannelFactory.getProbeType(mathIndex);
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit;
    }

    public void Unit(int mathIndex, String unit,boolean isUpdateUI){
        unit= unit.trim();
        if (unit.length()>3) unit= unit.substring(0,3);
        this.unit=unit;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_ADV_Unit);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(unit.trim());
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public String UnitQ(){
        return unit;
    }

}
