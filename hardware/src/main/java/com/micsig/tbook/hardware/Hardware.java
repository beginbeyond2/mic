package com.micsig.tbook.hardware;

import android.content.Context;
import android.net.INetdEventCallback;
import android.util.Log;

import java.nio.ByteBuffer;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class Hardware {
    private static final String TAG = "Hardware";
    private Context mContext;

    private SpiDev [] fpgaSpiDev = new SpiDev[2];

    private GpioDev fpgaSuspendIo;
    private GpioDev oeIo;
    private GpioDev powerIo;
    private GpioDev powerIo2;
    private GpioDev powerAdc2v5;
    private GpioDev powerAdc;
    private GpioDev powerPanel;
    private GpioDev powerPanel2;
    private GpioDev powerProbe;
    private GpioDev powerProbe2;
    private GpioDev probeIo;
    private GpioDev probeIo2;

    private GpioDev triggerInOut;
    private GpioDev clkInOut;

//    private GpioDev gpio_50VPwr;
    private SpiDevManager mSpiDevManager;
    private HwManager mHwManager;
    private HwLoadFpgaCode mHwLoadFpgaCode;
    private HwGpioManager mHwGpioManager;
    private ShiftRegister mShiftRegister;
    private Clk mClk;

    private static volatile Hardware instance = null;
    public static Hardware getInstance(){
        return instance;
    }
    public static Hardware getInstance(Context context) {
        if (instance == null) {
            synchronized (Hardware.class) {
                if (instance == null && context != null) {
                    instance = new Hardware(context);
                }
            }
        }
        return instance;
    }


    private Hardware(Context context){
        mContext = context;
        initHardware();

    }
    public int readE2PROM(int addr, byte[] byteArray)
    {
        return  mHwManager.readE2PROM(addr,byteArray);
    }
    public int writeE2PROM(int addr,byte[] byteArray)
    {
        int len = 0;
        len = mHwManager.writeE2PROM(addr,byteArray);
        return len;
    }
    public String getSysId(){
        return mHwManager.getSysId();
    }
    public void setFpgaDna(String dna)
    {
        mHwManager.setFpgaDna(dna);
    }

    public void standby(){
        powerAnalogOff();
    }
    public void resume(){
        powerAnalogOn();
    }
    public int getTemperature(){
        return mHwManager.getTemperature();
    }
    public int getCpuTemperature(){
        return mHwManager.getCpuTemperature();
    }


    public void setTemperature(int val){
        mHwManager.setTemperature(val);
    }
    public void setFanSpeed(int val){
        mHwManager.setFanSpeed(val);
    }

    //获得硬件版本
    public int getHwVersion(){
        return mHwManager.getHwVersion();
    }
    public int getSysVersion(){ return mHwManager.sysVersion();}

    public void probeIo(){
        probeIo.setVal(GpioDev.GPIO_VAL_HIGH);
    }
    public void setUsbInfo(String product,String serial,String ver){
        mHwManager.setUsbInfo(product,serial,ver);
    }

    //获取硬件通道数量 2, 4, 8通道
    public int getChNum(){
        return 8;
    }
       

    public boolean isAdcB12DJ3200NBB(){
        return (mHwManager.getHwVersion() & 0x01) != 0;
    }

//    public boolean isDac80501(){
//        return true;
//    }

    //获得机器UUID
    public String getMachineUUID(){
        return mHwManager.getMachineUUID();
    }

    //加载fpga
    public boolean loadFpgaCode(byte [] bytes){
        powerProbe.setVal(GpioDev.GPIO_VAL_HIGH);
        if(powerProbe2 != null){
            powerProbe2.setVal(GpioDev.GPIO_VAL_HIGH);
        }
        boolean bRet = mHwLoadFpgaCode.LoadFpgaCode(bytes);
        powerProbe.setVal(GpioDev.GPIO_VAL_LOW);
        if(powerProbe2 != null){
            powerProbe2.setVal(GpioDev.GPIO_VAL_LOW);
        }
        return bRet;
    }



    //向fpga发送命令
    public synchronized boolean sendFpgaCmd(int idx,ByteBuffer byteBuffer,int length){
        if(fpgaSpiDev[idx] != null) {
            fpgaSpiDev[idx].write(byteBuffer, 1);
            fpgaSpiDev[idx].write(byteBuffer, length);
            return true;
        }
        return false;
    }

    public synchronized void recvFpgaCmd(int idx,ByteBuffer wbyteBuffer,int length,ByteBuffer byteBuffer){
        if(fpgaSpiDev[idx] != null) {
            int s = fpgaSpiDev[idx].getSpeed();
            fpgaSpiDev[idx].setSpeed(15 * 1000 * 1000);
            fpgaSpiDev[idx].read(wbyteBuffer, length, byteBuffer);
            fpgaSpiDev[idx].setSpeed(s);
        }
    }




    public void sendShiftRegister(long val,int bits){
        Log.d("MHO38V1", "sendShiftRegister() called with: val = [" + Long.toHexString(val) + "], bits = [" + bits + "]");
        mShiftRegister.setVal(val,bits);
    }




    public void rstFpga(){
        mHwLoadFpgaCode.rstFpga();
    }
    public void sleepFpga(){
        fpgaSuspendIo.setVal(GpioDev.GPIO_VAL_HIGH);
    }
    public void wakeupFpga(){
        if(fpgaSuspendIo != null) {
            fpgaSuspendIo.setVal(GpioDev.GPIO_VAL_LOW);
        }
    }
    public void sleepClk(){
        //mClk.sleepClk();
    }

    public void wakeUpClk(){
        //mClk.wakeUpClk();
    }


    public void powerFpgaOn(){
        mHwLoadFpgaCode.powerOn();
    }
    public void powerFpgaOff(){
        mHwLoadFpgaCode.powerOff();
    }
    public void powerAnalogOn(boolean bFpga){
        if(bFpga){
            powerFpgaOn();
        }

        oeIo.setVal(GpioDev.GPIO_VAL_LOW);
        powerIo.setVal(GpioDev.GPIO_VAL_HIGH);
        if(powerIo2 != null){
            probeIo2.setVal(GpioDev.GPIO_VAL_HIGH);
        }
        if(powerAdc2v5 != null){
            powerAdc2v5.setVal(GpioDev.GPIO_VAL_HIGH);
        }
        powerAdc.setVal(GpioDev.GPIO_VAL_HIGH);

        powerPanel.setVal(GpioDev.GPIO_VAL_HIGH);
        if(powerPanel2 != null){
            powerPanel2.setVal(GpioDev.GPIO_VAL_HIGH);
        }

    }
    public void powerAnalogOn(){

        powerAnalogOn(true);
    }

    public void powerAnalogOff(boolean bFpga) {
        mHwLoadFpgaCode.standBy();
        powerPanel.setVal(GpioDev.GPIO_VAL_LOW);
        if(powerPanel2 != null){
            powerPanel2.setVal(GpioDev.GPIO_VAL_LOW);
        }
        if(powerAdc2v5 != null){
            powerAdc2v5.setVal(GpioDev.GPIO_VAL_LOW);
        }
        powerAdc.setVal(GpioDev.GPIO_VAL_LOW);
        powerIo.setVal(GpioDev.GPIO_VAL_LOW);
        if(powerIo2 != null){
            probeIo2.setVal(GpioDev.GPIO_VAL_LOW);
        }
        oeIo.setVal(GpioDev.GPIO_VAL_HIGH);
        if(bFpga){
            mHwLoadFpgaCode.powerOff();
        }
    }
    public void powerAnalogOff(){

        powerAnalogOff(true);
    }



    public void batteryProtect(){
//        batteryIo.setVal(GpioDev.GPIO_VAL_HIGH);
    }


    public double getChProbeRate(int chIdx){
        double ret = 1;
        int val = mHwManager.getChProbeVal(chIdx);
        if(val >= 1050 && val < 1340){
            ret = 10;
        }else if(val >= 840 && val < 1050){
            ret = 100;
        }
        return ret;
    }

    public boolean is5Oo(int chIdx){
//        if(chIdx == 0){
//            return (ch050o.getVal() == GpioDev.GPIO_VAL_HIGH);
//        }else if(chIdx == 1){
//            return (ch150o.getVal() == GpioDev.GPIO_VAL_HIGH);
//        }else{
//            return false;
//        }
        return false;
    }
    public boolean isAdOK(){
        //return (AdState.getVal() == GpioDev.GPIO_VAL_LOW);
        return false;
    }
    //
    private void initHardware(){

        mHwManager = HwManager.getInstance(mContext);
        mSpiDevManager = SpiDevManager.getInstance(mContext);
        mHwGpioManager = HwGpioManager.getInstance(mContext);
        mHwLoadFpgaCode = new HwLoadFpgaCode();
        mShiftRegister = new ShiftRegister();
        fpgaSpiDev[0] = mSpiDevManager.getSpiDev(SpiDevManager.SPI_DEV_FPGA1_CMD);
        fpgaSpiDev[1] = mSpiDevManager.getSpiDev(SpiDevManager.SPI_DEV_FPGA2_CMD);
        fpgaSuspendIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_FPGA_SUSPEND);
        oeIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_CH_MODEL_OE);
        powerIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ANALOG);
        powerIo2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ANALOG58);
        powerAdc = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_ADC);
        powerAdc2v5 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_ADC2V5_EN);
        powerPanel = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER_PANEL);
        powerPanel2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_POWER58_PANEL);
        powerProbe = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE_EN);
        powerProbe2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE58_EN);
        probeIo = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE_IO);
        probeIo2 = mHwGpioManager.getGpioDev(HwGpioManager.PIN_PROBE58_IO);
        triggerInOut = mHwGpioManager.getGpioDev(HwGpioManager.PIN_TRIG_CTL);
        clkInOut = mHwGpioManager.getGpioDev(HwGpioManager.PIN_10M_CLK_CTL);
//        gpio_50VPwr = mHwGpioManager.getGpioDev(HwGpioManager.PIN_50V_PWR);
        powerAnalogOn();//配置时钟需要先给模拟电打开
        wakeupFpga();

    }

    private void msleep(long ms){
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setTriggerInOut(boolean bIn){
        if(triggerInOut != null){
            triggerInOut.setVal(bIn ? GpioDev.GPIO_VAL_HIGH : GpioDev.GPIO_VAL_LOW);
        }
    }
    public void setClkInOut(boolean bIn){
        if(clkInOut != null){
            clkInOut.setVal(bIn ? GpioDev.GPIO_VAL_LOW : GpioDev.GPIO_VAL_HIGH);
        }
    }

}
