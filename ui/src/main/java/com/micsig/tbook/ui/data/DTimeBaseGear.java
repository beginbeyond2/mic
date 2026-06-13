package com.micsig.tbook.ui.data;

/**
 * Created by liwb on 2017/4/10.
 */

public class DTimeBaseGear {
    //region 单例
       private static class TimeBaseGearHolder{
           private static final DTimeBaseGear instance=new DTimeBaseGear();
       }

       public DTimeBaseGear(){}

      public static final DTimeBaseGear getInstance(){
        return TimeBaseGearHolder.instance;
      }
   //endregion

    //region  常量定义
    private static final int TimeGear_1Ks=0x01;
    private static final int TimeGear_500s=0x02;
    private static final int TimeGear_200s=0x03;
    private static final int TimeGear_100s=0x04;
    private static final int TimeGear_50s=0x05;
    private static final int TimeGear_20s=0x06;
    private static final int TimeGear_10s=0x07;
    private static final int TimeGear_5s=0x08;
    private static final int TimeGear_2s=0x09;
    private static final int TimeGear_1s=0x0a;
    private static final int TimeGear_500ms=0x0b;
    private static final int TimeGear_200ms=0x0c;
    private static final int TimeGear_100ms=0x0d;
    private static final int TimeGear_50ms=0x0e;
    private static final int TimeGear_20ms=0x0f;
    private static final int TimeGear_10ms=0x10;
    private static final int TimeGear_5ms=0x11;
    private static final int TimeGear_2ms=0x12;
    private static final int TimeGear_1ms=0x13;
    private static final int TimeGear_500us=0x14;
    private static final int TimeGear_200us=0x15;
    private static final int TimeGear_100us=0x16;
    private static final int TimeGear_50us=0x17;
    private static final int TimeGear_20us=0x18;
    private static final int TimeGear_10us=0x19;
    private static final int TimeGear_5us=0x1a;
    private static final int TimeGear_2us=0x1b;
    private static final int TimeGear_1us=0x1c;
    private static final int TimeGear_500ns=0x1d;
    private static final int TimeGear_200ns=0x1e;
    private static final int TimeGear_100ns=0x1f;
    private static final int TimeGear_50ns=0x20;
    private static final int TimeGear_20ns=0x21;
    private static final int TimeGear_10ns=0x22;
    private static final int TimeGear_5ns=0x23;
    private static final int TimeGear_2ns=0x24;
    private static final int TimeGear_1ns=0x25;
    //endregion

    //region  属性
    private int currTimeBaseGear=TimeGear_200ns;

    public int getCurrTimeBaseGear() {
        return currTimeBaseGear;
    }

    public void setCurrTimeBaseGear(int currTimeBaseGear) {
        this.currTimeBaseGear = currTimeBaseGear;
    }
    //endregion

   public String getTimeBaseGear_NumUnit(int timeBaseGear){
       String timeGear="50ms";
       switch(timeBaseGear){
           case TimeGear_1Ks:timeGear="1ks";break;
           case TimeGear_500s:timeGear="500s"; break;
           case TimeGear_200s:timeGear="200s"; break;
           case TimeGear_100s:timeGear="100s"; break;
           case TimeGear_50s: timeGear="50s";  break;
           case TimeGear_20s: timeGear="20s";  break;
           case TimeGear_10s: timeGear="10s";  break;
           case TimeGear_5s:  timeGear="5s";   break;
           case TimeGear_2s:  timeGear="2s";   break;
           case TimeGear_1s:  timeGear="1s";   break;

           case TimeGear_500ms:timeGear="500ms";break;
           case TimeGear_200ms:timeGear="200ms";break;
           case TimeGear_100ms:timeGear="100ms";break;
           case TimeGear_50ms: timeGear="50ms"; break;
           case TimeGear_20ms: timeGear="20ms"; break;
           case TimeGear_10ms: timeGear="10ms"; break;
           case TimeGear_5ms:  timeGear="5ms";  break;
           case TimeGear_2ms:  timeGear="2ms";  break;
           case TimeGear_1ms:  timeGear="1ms";  break;

           case TimeGear_500us:timeGear="500us"; break;
           case TimeGear_200us:timeGear="200us"; break;
           case TimeGear_100us:timeGear="100us"; break;
           case TimeGear_50us: timeGear="50us";  break;
           case TimeGear_20us: timeGear="20us";  break;
           case TimeGear_10us: timeGear="10us";  break;
           case TimeGear_5us:  timeGear="5us";   break;
           case TimeGear_2us:  timeGear="2us";   break;
           case TimeGear_1us:  timeGear="1us";   break;

           case TimeGear_500ns:timeGear="500ns"; break;
           case TimeGear_200ns:timeGear="200ns"; break;
           case TimeGear_100ns:timeGear="100ns"; break;
           case TimeGear_50ns: timeGear="50ns";  break;
           case TimeGear_20ns: timeGear="20ns";  break;
           case TimeGear_10ns: timeGear="10ns";  break;
           case TimeGear_5ns:  timeGear="5ns";   break;
           case TimeGear_2ns:  timeGear="2ns";   break;
           case TimeGear_1ns:  timeGear="1ns";   break;
           default:timeGear="50ms";break;
       }
       return timeGear;
   }

   public String getTimeBaseGear_Unit(int timeBaseGear){
      String unit="ms";
       switch(timeBaseGear){
           case TimeGear_1Ks:unit="ks";break;
           case TimeGear_500s:
           case TimeGear_200s:
           case TimeGear_100s:
           case TimeGear_50s:
           case TimeGear_20s:
           case TimeGear_10s:
           case TimeGear_5s:
           case TimeGear_2s:
           case TimeGear_1s:  unit="s";   break;

           case TimeGear_500ms:
           case TimeGear_200ms:
           case TimeGear_100ms:
           case TimeGear_50ms:
           case TimeGear_20ms:
           case TimeGear_10ms:
           case TimeGear_5ms:
           case TimeGear_2ms:
           case TimeGear_1ms:  unit="ms";  break;

           case TimeGear_500us:
           case TimeGear_200us:
           case TimeGear_100us:
           case TimeGear_50us:
           case TimeGear_20us:
           case TimeGear_10us:
           case TimeGear_5us:
           case TimeGear_2us:
           case TimeGear_1us:  unit="us";   break;

           case TimeGear_500ns:
           case TimeGear_200ns:
           case TimeGear_100ns:
           case TimeGear_50ns:
           case TimeGear_20ns:
           case TimeGear_10ns:
           case TimeGear_5ns:
           case TimeGear_2ns:
           case TimeGear_1ns:  unit="ns";   break;
           default:unit="ms";break;
       }
       return unit;
   }

}
