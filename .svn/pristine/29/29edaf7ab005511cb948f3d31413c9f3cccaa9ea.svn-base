package com.micsig.tbook.hardware;

import java.nio.ByteBuffer;

/**
 * Created by zhuzh on 2018/3/12.
 */

public class Clk {

    private static final String TAG = "CLK";
    private GpioDev mClkLE;
    private GpioDev mClkCE;
    private SpiDev mClkDev;

    public Clk(){
        HwGpioManager hwgpio = HwGpioManager.getInstance();
        mClkLE = hwgpio.getGpioDev(HwGpioManager.PIN_CLK_PLL_LE);
        mClkCE = hwgpio.getGpioDev(HwGpioManager.PIN_CLK_PLL_CE);
        mClkDev = SpiDevManager.getInstance().getSpiDev(SpiDevManager.SPI_DEV_CLK);
        clk_ini();
    }

    private void clk_ini(){
        mClkCE.setVal(GpioDev.GPIO_VAL_HIGH);

        mClkLE.setVal(GpioDev.GPIO_VAL_LOW);
        sendClkRegister(0xC9);
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH);

        mClkLE.setVal(GpioDev.GPIO_VAL_LOW);
        sendClkRegister(0x4FF1A0);
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH);

        mClkLE.setVal(GpioDev.GPIO_VAL_LOW);
        msleep(30);
        sendClkRegister(0xBB22);
//        sendClkRegister(0x7D02); //1G
        mClkLE.setVal(GpioDev.GPIO_VAL_HIGH);

        mClkLE.setVal(GpioDev.GPIO_VAL_LOW);
    }

    private void sendClkRegister(int val){
        byte [] buf = new byte[3];
        buf[0] = (byte)((val >>16) & 0xFF);
        buf[1] = (byte)((val >>8) & 0xFF);
        buf[2] = (byte)(val & 0xFF);

        mClkDev.write(ByteBuffer.wrap(buf),3);
    }

    private void msleep(long ms){
        try {

            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void sleepClk(){

        mClkCE.setVal(GpioDev.GPIO_VAL_LOW);
    }

    public void wakeUpClk(){

        mClkCE.setVal(GpioDev.GPIO_VAL_HIGH);
        msleep(10);
        clk_ini();
    }
}
