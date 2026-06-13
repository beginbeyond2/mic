package com.micsig.tbook.tbookscope.middleware.command;

import com.micsig.tbook.tbookscope.rxjava.RxBus;
import com.micsig.tbook.tbookscope.rxjava.RxEnum;
import com.micsig.tbook.tbookscope.scpi.SCPIParam;

/**
 * @auother Liwb
 * @description:
 * @data:2026-1-9 17:00
 */
public class Command_Measure_Setting {

//    new SCPICommandStruct(":MEASure:SETTing:INDicator","SCPI_Measure_Statistic","Indicator"), //
//    new SCPICommandStruct(":MEASure:SETTing:INDicator?","SCPI_Measure_Statistic","IndicatorQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:RANGe","SCPI_Measure_Statistic","Range"), //
//    new SCPICommandStruct(":MEASure:SETTing:RANGe?","SCPI_Measure_Statistic","RangeQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:ThReshold","SCPI_Measure_Statistic","Threshold"), //
//    new SCPICommandStruct(":MEASure:SETTing:ThReshold?","SCPI_Measure_Statistic","ThresholdQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:HIGH","SCPI_Measure_Statistic","High"), //
//    new SCPICommandStruct(":MEASure:SETTing:HIGH?","SCPI_Measure_Statistic","HighQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:MID","SCPI_Measure_Statistic","Mid"), //
//    new SCPICommandStruct(":MEASure:SETTing:MID?","SCPI_Measure_Statistic","MidQ"), //
//    new SCPICommandStruct(":MEASure:SETTing:LOW","SCPI_Measure_Statistic","Low"), //
//    new SCPICommandStruct(":MEASure:SETTing:LOW?","SCPI_Measure_Statistic","LowQ"), //


    private boolean isDisplay;
    private int rangeIndex;
    private int thresholdIndex;
    private String high;
    private String mid;
    private String low;
    public  void Indicator(boolean bDisplay, boolean isUpdateUI){
        this.isDisplay=bDisplay;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_Setting_Indicator);
            String param = String.valueOf(isDisplay);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  void Range(int index, boolean isUpdateUI){
        this.rangeIndex=index;
        if (isUpdateUI){
            CommandMsgToUI msgToUI = Command.get().getMsgToUI();
            msgToUI.setFlag(CommandMsgToUI.FLAG_Measure_Setting_Range);
            String param = String.valueOf(rangeIndex);
            msgToUI.setParam(param);
            RxBus.getInstance().post(RxEnum.COMMAND_TO_UI, msgToUI);
        }
    }
    public  void Threshold(int index, boolean isUpdateUI){
        this.thresholdIndex=index;
        if (isUpdateUI){

        }
    }
    public  void High(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) {
            this.high=value;
            if (isUpdateUI){

            }
        }
    }
    public  void Mid(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) {
            this.mid=value;
            if (isUpdateUI){

            }
        }
    }
    public  void Low(String value, boolean isUpdateUI){
        if (value.endsWith("%") || value.endsWith("V") || value.endsWith("Db")) {
            this.low=value;
            if (isUpdateUI){

            }
        }
    }



    public  boolean IndicatorQ(){
        return isDisplay;
    }
    public  int RangeQ(){
        return rangeIndex;
    }
    public  int ThresholdQ(){
        return thresholdIndex;
    }
    public  String HighQ(){
        return high;
    }
    public  String MidQ(){
        return mid;
    }
    public  String LowQ(){
        return low;
    }
}
