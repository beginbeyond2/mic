package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.base.Logger;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.scope.channel.MathChannel;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.tools.Tools;
import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.util.TBookUtil;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * @auother Liwb
 * @description:
 * @data:2022-3-25 14:44
 */
public class Command_Math_AXB {
//            new SCPICommandStruct(":MATH:AX+B:SOURce","SCPI_Math_AXB","Source"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:SOURce?","SCPI_Math_AXB","SourceQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:A","SCPI_Math_AXB","A"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:A?","SCPI_Math_AXB","AQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B","SCPI_Math_AXB","B"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:B?","SCPI_Math_AXB","BQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT","SCPI_Math_AXB","Unit"),//选择AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:UNIT?","SCPI_Math_AXB","UnitQ"),//查询AXB运算的信源
//            new SCPICommandStruct(":MATH:AX+B:EXTent","SCPI_Math_AXB","Extent"),//设置AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:EXTent?","SCPI_Math_AXB","ExtentQ"),//查询AXB运算结果的垂直档位
//            new SCPICommandStruct(":MATH:AX+B:OFFSet","SCPI_Math_AXB","Offset"),//设置AXB运算结果的垂直偏移
//            new SCPICommandStruct(":MATH:AX+B:OFFSet?","SCPI_Math_AXB","OffsetQ"),//查询AXB运算结果的垂直偏移

    private int ch;
    private double extent;
    private double offset;
    private String a="";
    private String b="";
    private String unit="";

    public void Source(int mathIndex, int ch, boolean isUpdateUI) {
        this.ch=ch;
        if (isUpdateUI) {
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Source);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(ch);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }

    }
    public  int SourceQ(){
        return this.ch;
    }

    private static final String ABunit[]={"k","p","n","u","m","0","1","2","3","4","5","6","7","8","9"};
    public  void A(int mathIndex, String a,boolean isUpdateUI){
        //判断输入合法性
        a=a.trim();
        boolean isNum=false;
        int index=-1;
        if (a.length()<=1){
            isNum = Tools.isNumeric(a);
            index=0;
        }else {
            String lastWord = a.substring(a.length() - 1);
            index = Tools.indexOf(ABunit, s -> lastWord.equals(s));
            if (Tools.isNumeric(lastWord)){
                isNum=Tools.isNumeric(a);
            }else {
                String num = a.substring(0, a.length() - 1);
                isNum = Tools.isNumeric(num);
            }

        }
//        Logger.i(Command.TAG,"lastword:"+lastWord+",num:"+num);
//        Logger.i(Command.TAG,"b:"+b+",index:"+index);
        if (isNum==false || index==-1) return;

        //超范围处理
        double d= TBookUtil.getDoubleFromM(a);
        a = TBookUtil.getValue(d);
        this.a=a;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_A);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(a);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  String AQ(){
        return a;
    }

    public void B(int mathIndex, String b, boolean isUpdateUI) {
        b=b.trim();
        boolean isNum=false;
        int index=-1;
        if (b.length()<=1){
            isNum = Tools.isNumeric(b);
            index=0;
        }else {
            String lastWord = b.substring(b.length() - 1);
            index = Tools.indexOf(ABunit, s -> lastWord.equals(s));
            if (Tools.isNumeric(lastWord)){
                isNum=Tools.isNumeric(b);
            }else {
                String num = b.substring(0, b.length() - 1);
                isNum = Tools.isNumeric(num);
            }
        }
        if (isNum==false || index==-1) return;
        double d= TBookUtil.getDoubleFromM(b);
        b = TBookUtil.getValue(d);
        this.b=b;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_B);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(b);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  String BQ(){
        return this.b;
    }

    public void Unit(int mathIndex, String unit, boolean isUpdateUI) {
        unit=unit.trim();
        if (unit.length()>3) unit=unit.substring(0,3);
        this.unit=unit;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_UNIT);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(unit);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  String UnitQ(){
        return this.unit;
    }


    public  void Extent(int mathIndex, double ext,boolean isUpdateUI){
//        if (this.extent==ext) return;
        this.extent=ext;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Extent);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(extent);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  double ExtentQ(){
        return this.extent;
    }
    public  void Offset(int mathIndex, double offset,boolean isUpdateUI){
//        if (this.offset==offset) return;
        this.offset=offset;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_MATH_AXB_Offset);
            String param = String.valueOf(mathIndex) + CommandMsgToUI.PARAM_SPLIT + String.valueOf(this.offset);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  String OffsetQ(int mathIndex){
        MathChannel channel = ChannelFactory.getMathChannel(mathIndex);
        double pos;
//        switch (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_TYPE)) {
//            case CacheUtil.MATHTYPE_DW:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_DW_Y_POSITION);
//                break;
//            case CacheUtil.MATHTYPE_FFT:
//                if (CacheUtil.get().getInt(CacheUtil.RIGHT_SLIP_MATH_FFT_TYPE) == 0) {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_RMS_Y_POSITION);
//                } else {
//                    pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_FFT_DB_Y_POSITION);
//                }
//                break;
//            case CacheUtil.MATHTYPE_AXB:
                pos = CacheUtil.get().getDouble(CacheUtil.MAIN_WAVE_MATH_AXB_Y_POSITION + TChan.toUiChNo(mathIndex));
//                break;
//            default:
//                pos = CacheUtil.get().getInt(CacheUtil.MAIN_WAVE_MATH_AM_Y_POSITION);
//                break;
//        }
        pos = GlobalVar.get().getMainWave().y / 2 - pos;
        String unit = ChannelFactory.getProbeType(mathIndex);
        return TBookUtil.getFourFromD_Trim0(pos * channel.getADVerticalPerPix()) + unit;
    }
}
