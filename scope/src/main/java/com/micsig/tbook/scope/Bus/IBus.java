package com.micsig.tbook.scope.Bus;

import com.micsig.tbook.scope.channel.ChannelFactory;

/**
 * Created by zhuzh on 2018-5-29.
 */

public abstract class IBus {

    //Bus Type
    public static final int UART = 0;
    public static final int LIN = 1;
    public static final int CAN = 2;
    public static final int SPI = 3;
    public static final int I2C = 4;
    public static final int ARINC429 = 5;
    public static final int MILSTD1553B = 6;
    public static final int CAN_FD = 7;
    public static final int BUS_CNT = 8;
    //Idle level
    public static final int IDLE_LEVEL_HIGH = 0;
    public static final int IDLE_LEVEL_LOW  = 1;
    //diaplay

    public static final int DISPLAY_BIN_DISPLAY = 0;
    public static final int DISPLAY_HEX_DISPLAY = 1;
    public static final int DISPLAY_ASC_DISPLAY = 2;
    //relation
    public static final int TRIGGER_RELATION_EQUAL      = 0;
    public static final int TRIGGER_RELATION_MORE_THAN  = 1;
    public static final int TRIGGER_RELATION_LESS_THAN  = 2;
    public static final int TRIGGER_RELATION_NOT_EQUAL  = 3;

    private int busType = UART;
    private int busIdx = ChannelFactory.S1;
    private BusAction busAction;
    public IBus(int busIdx,int busType){
        this.busIdx = busIdx;
        this.busType = busType;
        busAction = new BusAction(this);
    }
    public int getBusType(){
        return busType;
    }
    public static boolean isValid(int busType){
        return (busType >= UART && busType < BUS_CNT);
    }

    public int getBusIdx() {
        return busIdx;
    }

    protected void busChange(){

        busAction.busChange();
    }
    protected void chChange(){
        busAction.chSampleChange();
    }
    protected void busTypeChange(){
        busAction.busTypeChange();
    }
    public abstract int getChSampleCnt();
    public abstract boolean isChInSample(int chIdx);

    private static boolean [] busEnable = {false,false,false,false,false,false,false,false};
    public static void setBusEnable(int busType,boolean bEnable){
        if(isValid(busType)){
            busEnable[busType] = bEnable;
        }
    }
    public static boolean isBusEnable(int busType){
        if(isValid(busType)){
            return busEnable[busType];
        }
        return false;
    }
}
