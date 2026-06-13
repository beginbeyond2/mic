package com.micsig.tbook.hardware;

import android.util.Log;

/**
 * Created by zhuzh on 2018/3/12.
 */

public class ShiftRegister {

    private GpioDev mChModelDs;
    private GpioDev mChModelSrclr;
    private GpioDev mChModelSrclk;
    private GpioDev mChModelRclk;
    public ShiftRegister(){
        HwGpioManager hwgpio = HwGpioManager.getInstance();
        mChModelDs = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_DS);
        mChModelSrclr = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_SRCLR);
        mChModelSrclk = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_SRCLK);
        mChModelRclk = hwgpio.getGpioDev(HwGpioManager.PIN_CH_MODEL_RCLK);
        init();
    }



    public void setVal(long val,int bits){
        Log.d("ShiftRegister", "setVal() called with: val = [" + Long.toHexString(val) + "], bits = [" + bits + "]");
        mChModelSrclr.setVal(GpioDev.GPIO_VAL_LOW);
        mChModelSrclr.setVal(GpioDev.GPIO_VAL_HIGH);
        mChModelSrclk.setVal(GpioDev.GPIO_VAL_LOW);
        mChModelRclk.setVal(GpioDev.GPIO_VAL_LOW);

        for(int i=0;i<bits;i++){
            mChModelDs.setVal(((val>>>i) & 0x01) == 0 ? GpioDev.GPIO_VAL_LOW : GpioDev.GPIO_VAL_HIGH);
            mChModelSrclk.setVal(GpioDev.GPIO_VAL_HIGH);
            mChModelSrclk.setVal(GpioDev.GPIO_VAL_LOW);
        }
        mChModelRclk.setVal(GpioDev.GPIO_VAL_HIGH);
        mChModelRclk.setVal(GpioDev.GPIO_VAL_LOW);
    }

    private void init(){
        setVal(0xFFFFFFFFFFFFFFFFL,64);
    }
}
