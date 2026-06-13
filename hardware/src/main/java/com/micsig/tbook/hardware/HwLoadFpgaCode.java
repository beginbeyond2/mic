package com.micsig.tbook.hardware;

import android.util.Log;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class HwLoadFpgaCode {

    private static final String TAG = "HwLoadFpgaCode";
    private SpiDev mDev;
    private GpioDev gpio_nConfig;
    private GpioDev gpio_nStatus;
    private GpioDev gpio_ConfigDone;
    private GpioDev gpio_FpgaRst;


    private GpioDev gpio_vcc1v0;
    private GpioDev gpio_vcc1v2;
    private GpioDev gpio_vcc1v8;
    private GpioDev gpio_vcc3v3;

    private GpioDev gpio_vcc3v3Ext;


    public HwLoadFpgaCode(){
        mDev = SpiDevManager.getInstance().getSpiDev(SpiDevManager.SPI_DEV_FPGA_BOOT);
        HwGpioManager hwGpio = HwGpioManager.getInstance();
        gpio_nConfig = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nCONFIG);
        gpio_nStatus = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nSTATUS);
        gpio_ConfigDone = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_CONFIG_DONE);
        gpio_FpgaRst = hwGpio.getGpioDev(HwGpioManager.PIN_FPGA_nRST);

        gpio_vcc1v0 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V0);
        gpio_vcc1v2 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V2);
        gpio_vcc1v8 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC1V8);
        gpio_vcc3v3 = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC3V3);
        gpio_vcc3v3Ext = hwGpio.getGpioDev(HwGpioManager.FPGA_VCC3V3_EXT);

    }

    public void powerOn(){
        gpio_vcc1v0.setVal(GpioDev.GPIO_VAL_HIGH);
        gpio_vcc1v2.setVal(GpioDev.GPIO_VAL_HIGH);
        gpio_vcc1v8.setVal(GpioDev.GPIO_VAL_HIGH);
        gpio_vcc3v3.setVal(GpioDev.GPIO_VAL_HIGH);
        gpio_vcc3v3Ext.setVal(GpioDev.GPIO_VAL_HIGH);
    }
    public void powerOff(){
        gpio_vcc3v3Ext.setVal(GpioDev.GPIO_VAL_LOW);
        gpio_vcc3v3.setVal(GpioDev.GPIO_VAL_LOW);
        gpio_vcc1v8.setVal(GpioDev.GPIO_VAL_LOW);
        gpio_vcc1v2.setVal(GpioDev.GPIO_VAL_LOW);
        gpio_vcc1v0.setVal(GpioDev.GPIO_VAL_LOW);
    }

    private final static int SPI_MAX_FPGA_CODE = 15872;



    public boolean LoadFpgaCode(byte [] bytes){

        int cnt = 300000;
        boolean ok = false;
        int N = 2;
//        Log.d(TAG,"len:" + bytes.length);
        do
        {

            gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH);

            usleep(2 * N);
            gpio_nConfig.setVal(GpioDev.GPIO_VAL_LOW);

            usleep(10 * N );//等待>2us
            if(gpio_nStatus.getVal() == GpioDev.GPIO_VAL_HIGH)
            {
                gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH);

                usleep(2000 * N);
                Log.e(TAG,"fpga code load false,nstatus is high when nconfig go to low!");
                continue;
            }//err;

            gpio_nConfig.setVal(GpioDev.GPIO_VAL_HIGH);

            usleep(1000 * N * 5);//大于300uS，实测需要大于500us
            if(gpio_nStatus.getVal() == GpioDev.GPIO_VAL_LOW)
            {
                usleep(2000 * N);
                Log.e(TAG,"fpga code load false,nstatus is still low when nconfig go to high!");
                continue;
            }//err;
            //Step2.配置数据 数据.低位先，高位后
            //      DCLK为高前必须准备好数据
            usleep(2 * N);//等待>2us

            int wlen = 0;
            int once = SPI_MAX_FPGA_CODE;
            byte [] in = new byte[SPI_MAX_FPGA_CODE + 8];
            int addr = 0;
            int addrNBytes = mDev.getAddrNBytes();
            mDev.setAddr(addr);
            while (wlen < bytes.length){
                once = bytes.length - wlen;
                if(once > SPI_MAX_FPGA_CODE) {
                    once = SPI_MAX_FPGA_CODE;
                }
                System.arraycopy(bytes,wlen,in,addrNBytes,once);
                if((once & 0x3) != 0){
                    once += 4 -(once & 0x3);
                }
                mDev.write(ByteBuffer.wrap(in),once + addrNBytes);
                wlen += once;
                mDev.setAddr(addr++);
            }
            for(int i=0;i<8;i++){
                in[i] = 0;
            }
            usleep(2000 * N);//200us测试了，有些板子不行
            int nums = 0;
            while (gpio_ConfigDone.getVal() == GpioDev.GPIO_VAL_LOW && nums < 255){
                mDev.write(ByteBuffer.wrap(in),4 + addrNBytes);
                mDev.setAddr(addr++);
                usleep(2000 * N);
                nums++;
            }

            if (gpio_ConfigDone.getVal() == GpioDev.GPIO_VAL_LOW) {
                usleep(2000 * N);
                Log.e(TAG, "fpga code load false,configDone is low when all datas is load!");
                continue;
            }

            usleep(2000 * N);//,叮咚让改成2ms
            rstFpga();
            ok = true;

            Log.d(TAG,"fpga code load ok");
            usleep(5000);//,杨工让添加5ms
            break;
        }while(--cnt!=0);
        return ok;
    }

    private void usleep(int us){
        try {

            Thread.sleep(us/1000,(us % 1000)*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void rstFpga(){
        gpio_FpgaRst.setVal(GpioDev.GPIO_VAL_LOW);
        usleep(1000);
        gpio_FpgaRst.setVal(GpioDev.GPIO_VAL_HIGH);
    }
    public void standBy(){
        gpio_nConfig.setVal(GpioDev.GPIO_VAL_LOW);
    }


}
