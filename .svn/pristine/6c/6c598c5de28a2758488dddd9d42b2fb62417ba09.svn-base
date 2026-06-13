package com.micsig.tbook.scope.Bus;

/**
 * Created by zhuzh on 2018-5-29.
 */

public class UartBus extends IBus {

    //verify
    public static final int UART_NONE_VERIFY = 0;
    public static final int UART_ODD_VERIFY = 1;
    public static final int UART_EVEN_VERIFY = 3;

    //trigger Type
    public static final int UART_TRIGGER_START_BIT          = 1;
    public static final int UART_TRIGGER_STOP_BIT           = 2;
    public static final int UART_TRIGGER_DATA               = 3;
    public static final int UART_TRIGGER_DATA1              = 4;
    public static final int UART_TRIGGER_DATA0              = 5;
    public static final int UART_TRIGGER_DATAx              = 6;
    public static final int UART_TRIGGER_OOD_EVEN_BIT_ERROR = 7;
    public static final int UART_TRIGGER_TYPE_MAX = 8;



    private int rxChIdx = 0;
    private int idleLevel = IDLE_LEVEL_HIGH;
    private int verify = UART_NONE_VERIFY;
    private int bits = 0;
    private int baudRate = 9600;
    private int displayFormat = DISPLAY_HEX_DISPLAY;
    private int triggerType = UART_TRIGGER_START_BIT;
    private int []triggerDatas = new int[UART_TRIGGER_TYPE_MAX];
    private int triggerRelation = TRIGGER_RELATION_EQUAL;

    public static boolean isTriggerTypeValid(int uartTriggerType){
        return (uartTriggerType>=UART_TRIGGER_START_BIT && uartTriggerType<=UART_TRIGGER_OOD_EVEN_BIT_ERROR);
    }
    public UartBus(int busIdx) {
        super(busIdx,UART);
    }
    public void setRxChIdx(int chIdx){
        synchronized (this) {
            this.rxChIdx = chIdx;
        }
        chChange();
    }
    public synchronized int getRxChIdx(){
        return rxChIdx;
    }
    public void setIdleLevel(int idleLevel){
        this.idleLevel = idleLevel;
        busChange();
    }
    public int getIdleLevel(){
        return idleLevel;
    }
    public void setVerify(int verify){
        this.verify = verify;
        busChange();
    }
    public int getVerify(){
        return verify;
    }
    public void setBits(int bits){
        this.bits = bits;
        busChange();
    }
    public int getBits(){
        return bits;
    }
    public void setBaudRate(int baudRate){
        this.baudRate = baudRate;
        busChange();
    }
    public int getBaudRate(){
        return baudRate;
    }
    public void setDisplayFormat(int displayFormat){
        this.displayFormat = displayFormat;
    }
    public int getDisplayFormat(){
        return displayFormat;
    }
    public void setTriggerType(int triggerType){
        if(isTriggerTypeValid(triggerType)) {
            this.triggerType = triggerType;
            busChange();
        }
    }

    public int getTriggerType(){
        return triggerType;
    }

    public void setTriggerData(int val){
        triggerDatas[triggerType] = val;
        busChange();
    }
    public int getTriggerData(){
        return triggerDatas[triggerType];
    }
    public void setTriggerData(int triggerType,int val){
        if(isTriggerTypeValid(triggerType)) {
            triggerDatas[triggerType] = val;
            busChange();
        }
    }
    public int getTriggerData(int triggerType){
        if(isTriggerTypeValid(triggerType))
            return triggerDatas[triggerType];
        return 0;
    }

    public void setTriggerRelation(int triggerRelation){
        this.triggerRelation = triggerRelation;
        busChange();
    }
    public int getTriggerRelation(){
        return triggerRelation;
    }

    @Override
    public int getChSampleCnt() {
        return 1;
    }

    @Override
    public boolean isChInSample(int chIdx) {
        synchronized (this) {
            return rxChIdx == chIdx;
        }
    }
}
