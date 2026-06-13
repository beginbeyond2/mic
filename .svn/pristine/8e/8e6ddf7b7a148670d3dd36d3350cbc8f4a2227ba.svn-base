package com.micsig.tbook.tbookscope.scpi;

/**
 * Created by liwb on 2018/8/9.
 */

public class ToolsSCPI {

    public static boolean isCorrect(int index, int[] src) {
        return index >= 0 && index < src.length;
    }

    public static boolean isCorrect(int index, double[] src) {
        return index >= 0 && index < src.length;
    }

    public static boolean isCorrect(int index, long[] src) {
        return index >= 0 && index < src.length;
    }

    public static boolean isCorrect(int index, boolean[] src) {
        return index >= 0 && index < src.length;
    }

    public static String getOKAY(){return "OKAY";}
    public static String getSuccState(boolean b){return b?"successful":"failed";}
    public static String getOpenStateToInt(boolean b){return b?"1":"0";}

//    public static String getOpenState(boolean b){
//        return b?"1|ON":"0|OFF";
//    }
    public static String getOpenState(boolean b){
    return b?"1":"0";
}

    public static String getDouble(double d){
        return String.valueOf(d);
    }

    public static String getString(String s){return s;}

    public static String getInt(int i){return String.valueOf(i);}

    public static String getUnSCPI(){return "Unrealized SCPI";}

    private static final String[] Ch = {"CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8"};
    public static String getCh(int index){ return Ch[index];}

    private static final String[] chAll = {
            "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8",
            "MATH1", "MATH2", "MATH3", "MATH4", "MATH5", "MATH6", "MATH7", "MATH8",
            "R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8",
            "S1", "S2", "S3", "S4","OFF","EXT"};
    public static String getChAll(int index){return chAll[index];}

    private static final String[] counter_ch = {"CLOSe", "CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8"};
    public static String getCounterCh(int index){return counter_ch[index];}

    private static final String[] band={"FULL","200M","20M","HIGH","LOW","NULL"};
    public static String getBand(int index){ return band[index];}

    private static final String[] prty={"VOL","CUR"};
    public static String getPrty(int index){return prty[index];}

    private static final String[] probe={"0.001","0.002","0.005","0.01","0.02","0.05",
            "0.1","0.2","0.5","1","2","5","10","20","50","100","200","500",
            "1000","2000","5000","10000"};
    public static  String getProbe(int index){return probe[index];}

    private static final String[] couple={"DC","AC","GND"};
    public static String getCouple(int index){ return couple[index];}

    private static final String[] inputres={"MEGA","FIFTy"};
    public static String getInputres(int index){return inputres[index];}

    private static final String[] sampleType={"NORMal","MEAN","ENVelop","PEAK","HIGHres","SEGMented"};
    public static String getSampleType(int index){return sampleType[index];};

    private static final String[] sampleEnvelop={"2","4","8","16","32","64","128","256","inf"};
    public static String getSampleEnvelop(int index){return sampleEnvelop[index];}

    private static final String[] mathMode={"BASE","FFT", " AX+B","ADVAnced"};
    public static String getMathMode(int index){return  mathMode[index];}

    private static final String[] mathVREF={"Center","Zero"};
    public static String getMathVRef(int index){return mathVREF[index];}

    private static final String[] mathBaseOperator={"ADD","SUB","MUL","DIV"};
    public static String getMathBaseOperator(int index){return mathBaseOperator[index];}

    private static final String[] mathWindow={"RECTangle","HAMMing","BLACkman","HANNing"};
    public static String getMathWindow(int index){return mathWindow[index];}

    private static final String[] mathFftType={"LINE","DB"};
    public static String getMathFftType(int index){return mathFftType[index];}

    private static final String[] displayWaveForm={"DOTS","VECTors"};
    public static String getDisplayWaveForm(int index){ return displayWaveForm[index];}

    private static final String[] displayBackground={"Dark","Light"};

    public static String getDisplayBackground(int index){return displayBackground[index];}

    private static final String[] displayGraticule={"FULL","GRID","RETical","FRAMe"};
    public static String getDisplayGraticule(int index){ return displayGraticule[index];    }

    private static final String[] displayPersistMode={"none","AUTO","NORMal","INFinite"};
    public static String getDisplayPersistMode(int index){return displayPersistMode[index];}

    private static final String[] displayFftPersistMode={"none","AUTO","NORMal","INFinite"};
    public static String getDisplayFftPersistMode(int index){return displayFftPersistMode[index];}

    public static final Integer[] displayPersistAdjust={100,200,300,400,500,600,700,800,900,1000,2000,3000,4000,5000,6000,7000,8000,9000,10000};
    public static String getDisplayPersistAdjust(int index){ return  String.valueOf(displayPersistAdjust[index]);}

    public static final Integer[] displayFftPersistAdjust = {200, 500, 1000, 2000, 5000, 10000};
    public static String getDisplayFftPersistAdjust(int index){ return  String.valueOf(displayFftPersistAdjust[index]);}

    private static final String[] displayHorRef={"CENTer","TRIGpos"};
    public static String getDisplayHorRef(int index){ return displayHorRef[index];}

    private static final String[] triggerType={"COMMon","EDGE", "PULSe", "LOGic", "NEDGe", "RUNT", "SLOPe", "TIMeout", "VIDeo","S1","S2","S3","S4"};
    public static String getTriggerType(int index){return  triggerType[index];}

    private static final String[] triggerMode={"AUTO","NORMal"};
    public static String getTriggerMode(int index){return  triggerMode[index];}

    private static final String[] triggerStatus={"STOP","RUN","WAIT","AUTO"};
    public static String getTriggerStatus(int index){return  triggerStatus[index];}

    private static final String[] triggerEdgeSlope={"RISE","FALL","DUAL"};
    public static String getTriggerEdgeSlope(int index){return triggerEdgeSlope[index];}

    private static final String[] triggerEdgeCouple={"DC","AC","HFRej","LFRej","Noiserej"};
    public static String getTriggerEdgeCouple(int index){return triggerEdgeCouple[index];}

    private static final String[] triggerPulsePolarity={"POSitive","NEGative","EITHer"};
    public static String getTriggerPulsePolarity(int index){return  triggerPulsePolarity[index];}

    private static final String[]  triggerPulseCondition={"LESS","GREat","EQUal","UNEQual"};
    public static String getTriggerPulseCondition(int index){return triggerPulseCondition[index];}

    private static final String[] triggerLogicStatus={"HIGH","LOW","NONE"};
    public static String getTriggerLogicStatus(int index){return  triggerLogicStatus[index];}

    private static final String[] triggerLogicFunction={"AND","OR","NAND","NOR"};
    public static String getTriggerLogicFunction(int index){return  triggerLogicFunction[index];}

    private static final String[] triggerLogicCondition={"LESS","GREat","EQUal","UNEQual","TRUE","FALSe"};
    public static String getTriggerLogicCondition(int index){return  triggerLogicCondition[index];}

    private static final String[] triggerDwartCondition={"LESS","GREAt","BETWeen","NONE"};
    public static String getTriggerDwartCondition(int index){return triggerDwartCondition[index];}

    private static final String[] triggerSlopeEdge={"RISE","FALL","EITHer"};
    public static String getTriggerSlopeEdge(int index){return triggerSlopeEdge[index];}

    private static final String[] triggerSlopeCondition={"LESS","GREat","BETWeen"};
    public static String getTriggerSlopeCondition(int index){return triggerSlopeCondition[index];}

    private static final String[] triggerTimeoutPolarity={"POSitive","NEGative","EITHer"};
    public static String getTriggerTimeoutPolarity(int index){return triggerTimeoutPolarity[index];}

    private static final String[] triggerNedgeSlope={"RISE","FALL","EITHer"};
    public static String getTriggerNedgeSlope(int index){return  triggerNedgeSlope[index];}
    private static final String[] triggerRiseFall={"RISE","FALL"};
    public static String getTriggerRiseFall(int idx){return triggerRiseFall[idx];}
    private static final String[] triggerVideoPolarity={"POSitive","NEGative"};
    public static String getTriggerVideoPolarity(int index){return triggerVideoPolarity[index];}

    private static final String[] triggerVideoStandard={"PAL","SECAm","NTSC","720P","1080I","1080P"};
    public static String getTriggerVideoStandard(int index){return triggerVideoStandard[index];}

    private static final String[] triggerVideoAmode={"ODDField","EVENfield","ALLField","ALLLine","LINE"};
    public static String getTriggerVideoAmode(int index){return triggerVideoAmode[index];}

    private static final String[] triggerVideoBmode={"ALLField","ALLLine","LINE"};
    public static String getTriggerVideoBmode(int index){return triggerVideoBmode[index];}

    private static final String[] triggerVideoAfrequence={"60Hz","50Hz"};
    public static String getTriggerVideoAfrequence(int index){return triggerVideoAfrequence[index];}

    private static final String[] triggerVideoBfrequence={"60Hz","50Hz","30Hz","25Hz","24Hz"};
    public static String getTriggerVideoBfrequence(int index){return triggerVideoBfrequence[index];}

    private static final String[] timebaseMode={"YT","XY"};
    public static String getTimebaseMode(int index){return timebaseMode[index];}

    private static final String[] local={"Local","UDisk"};
    public static String getLocal(int index){ return local[index]; }

    private static final String[] saveType={"WAV","CSV","BIN"};
    public static String getSaveType(int index){return saveType[index];}

    private static final String[] busType={"Uart","LIN","CAN","SPI","I2C","429","1553B"};
    public static String getBusType(int index){return busType[index];}

    private static final String[] idLevel={"high","low"};
    public static String getIdLevel(int index){return idLevel[index];}

    private static final String[] linBaudRate={"2400","4800","9600","19200"};
    public static String getLinBaudRate(int index){return  linBaudRate[index];}

    private static final String[] uartBaudRate={"1200","2400","4800","9600","19200","38400","43000","56000","57600","115200"};
    public static String getUartBaudRate(int index){return uartBaudRate[index];}

    private static final String[] uartCheck={"NONE","ODD","EVEN"};
    public static String getUartCheck(int index){return uartCheck[index];}

    private static final String[] uartWidth={"5","6","7","8","9"};
    public static String getUartWidth(int index){return uartWidth[index];}

    private static final String[] uartDisplay={"Hex","Bin","ASCII"};
    public static String getUartDisplay(int index){return uartDisplay[index];}

    private static final String[] canSignal={"CAN_H","CAN_L","H_L","L_H","Rx","Tx"};
    public static String getCanSignal(int index){return canSignal[index];}

    public static final String[] canBaudRate={"100000","500000","1000000"};
    public static String getCanBaudRate(int index){ return canBaudRate[index];}

    public static final String[] CanFDBaudrate={"NONE","2000000","5000000"};
    public static String getCanFDBaudrate(int index){return CanFDBaudrate[index];};

    public static final int[] canDlc={0,1,2,3,4,5,6,7,8,12,16,20,24,32,48,64};
    public static final int getCanDlc(int index){return canDlc[index];}

    public static final String[] canIso={"ISO","NON"};
    public static final String getCanIso(int index){return canIso[index];}

    private static final String[] art429Format={"LABEL+DATA","L+D+SSM","L+SDI+D+SSM"};
    public static final String get429Format(int index){ return art429Format[index];}

    private static final String[] art429Display={"Bin","Hex"};
    public static final String get429Display(int index){return art429Display[index];}

    private static final String[] art429BaudRate={"12500","100000"};
    public static final String get429BaudRate(int index){return art429BaudRate[index];}

    private static final String[] uartTriggerType={"STARt","STOP","DATA","0:DATA","1:DATA","X:DATA","PARIty"};
    public static final String getuartTriggerType(int index){return uartTriggerType[index];}

    private static final String[] LinTriggerType={"SRISe","FID","IDATa"};
    public static final String getLinTriggerType(int index){return LinTriggerType[index];}

    private static final String[] CanTriggerType={"FSTArt","RFID","DFID","RDID","IDATa","WRFR","AERRor","ACKError","OVERload"};
    public static final String getCanTriggerType(int index){return CanTriggerType[index];}

    private static final String[] SPITriggerType={"CS","DATA","X:DATA"};
    public static final String getSpiTriggerType(int index){return SPITriggerType[index];}

    private static final String[] IICTriggerType={"STARt","STOP","ACKLost","RESTart","NACKaddress","FRAM1","FRAM2","RDATa","WRITe10",};
    public static final String getIICTriggerType(int index){return IICTriggerType[index];}

    private static final String[] B1553bTriggerType={"CSSYnc","DWSYnc","CSWOrd","RTADdress","MERRor","DWORd","OPERror","AERRor",};
    public static final String getB1553bTriggerType(int index){return B1553bTriggerType[index];}

    private static final String[] Arinc429TriggerType={"WBEGin","WEND","LABEl","SDI","DATA","SSM","LSDI","LDATa","LSSM","WERROr","WINTerval","VERRor","AERRor","ALL0","ALL1"};
    public static final String getArinc429TriggerType(int index){return Arinc429TriggerType[index];}

    private static final String[] SerialTriggerCondition={"LESS","GREAt","EQUAl","UNEQual"};
    public static final String getSerialTriggerCondition(int index){return SerialTriggerCondition[index];}

    private static final String[] CountMode={"FREQuency","PERiod","TOTalize"};
    public  static final String getCountMode(int index){return CountMode[index];}

    private static final String[] mDepth={ "Auto", "AUTO","220000000","22000000","2200000","220000","22000","110000000","11000000","1100000","110000","11000",};
    public static final String getMdepth(int index){return mDepth[index];}

    private static final String[] mAutoSource={"CURRent","Max"};
    public static final String getAutoSource(int index){return mAutoSource[index];}

    private static final String[] mSerialBusMode={"GRAP","TXT"};
    public static final String getSerialBusMode(int index){ return mSerialBusMode[index];}

    private static final String[] mSpiBits={ "4","8","16","24","32",};
    public static final String getSpiBits(int index){return mSpiBits[index];}

    private static final String[] mSegmentedDisplayType={"SINGLe","FIT"};
    public static final String getSegmentDisplayType(int index){return mSegmentedDisplayType[index];}

    private static final String[] mSegmentDisplaySpeed={"1","2","4","8"};
    public static final String getSegmentDisplaySpeed(int index){return mSegmentDisplaySpeed[index];}

    private static final String[] mSegmentOrder={"ORDer","REORder"};
    public static final String getSegmentOrder(int index){return mSegmentOrder[index];}

    private static final String[] mWaveFormMode={"NORMal","MAXimum","RAW"};
    public static final String getWaveFormMode(int index){return mWaveFormMode[index];}

    private static final String[] mWaveFormFormat={"WORD","BYTE","ASCii "};
    public static final String getWaveFormFormat(int index){return mWaveFormFormat[index];}

    private static final String[] mAux={"OUT","IN"};
    public static final String getAux(int index){return mAux[index];}

    private static final String[] mMeasureSettingRange={"SCReen","CURSor"};
    public static final String getMeasureSettingRange(int index){return mMeasureSettingRange[index];}
    private static final String[] mMeasureSettingThreshold={"PERCent","ABSolute"};
    public static final String getMeasureSettingThreshold(int index){return mMeasureSettingThreshold[index];}

}
