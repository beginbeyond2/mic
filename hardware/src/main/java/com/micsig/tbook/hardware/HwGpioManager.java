package com.micsig.tbook.hardware;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.GpioManager;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class HwGpioManager {

    private static final String TAG = "HwGpioManager";
    private Context mContext;
    private GpioManager mGpioManager;

    @IntDef({PIN_FPGA_nCONFIG,
            PIN_FPGA_CONFIG_DONE,PIN_FPGA_nSTATUS,PIN_FPGA_nRST,PIN_FPGA_ACK12,
            PIN_POWER_ANALOG,PIN_CH_MODEL_OE,PIN_CH_MODEL_DS,PIN_CH_MODEL_SRCLR,
            PIN_CH_MODEL_SRCLK,PIN_CH_MODEL_RCLK,PIN_FPGA_SUSPEND,
            PIN_FPGA_AWAKE,PIN_POWER_ADC,PIN_POWER_PANEL,PIN_CLK_PLL_LE,PIN_CLK_PLL_CE,PIN_PROBE_EN,
            FPGA_VCC1V0,FPGA_VCC1V2,FPGA_VCC1V8,FPGA_VCC3V3,PIN_PROBE_IO,PIN_POWER_ANALOG58,
            PIN_FPGA_AWAKE2,PIN_ADC2V5_EN,PIN_POWER58_PANEL,PIN_PROBE58_EN,PIN_PROBE58_IO,
            PIN_TRIG_CTL,PIN_10M_CLK_CTL,PIN_50V_PWR

    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIO_PIN {}

    public static final int PIN_FPGA_nCONFIG = 0;
    public static final int PIN_FPGA_CONFIG_DONE = PIN_FPGA_nCONFIG + 1;
    public static final int PIN_FPGA_nSTATUS = PIN_FPGA_CONFIG_DONE + 1;
    public static final int PIN_FPGA_nRST = PIN_FPGA_nSTATUS + 1;
    public static final int PIN_FPGA_ACK12 = PIN_FPGA_nRST + 1;
    public static final int PIN_POWER_ANALOG = PIN_FPGA_ACK12 + 1;
    public static final int PIN_POWER_ANALOG58 = PIN_POWER_ANALOG + 1;
    public static final int PIN_CH_MODEL_OE = PIN_POWER_ANALOG58 + 1;
    public static final int PIN_CH_MODEL_DS = PIN_CH_MODEL_OE + 1;
    public static final int PIN_CH_MODEL_SRCLR = PIN_CH_MODEL_DS + 1;
    public static final int PIN_CH_MODEL_SRCLK = PIN_CH_MODEL_SRCLR + 1;
    public static final int PIN_CH_MODEL_RCLK = PIN_CH_MODEL_SRCLK + 1;
    public static final int PIN_FPGA_SUSPEND = PIN_CH_MODEL_RCLK + 1;
    public static final int PIN_FPGA_AWAKE = PIN_FPGA_SUSPEND + 1;
    public static final int PIN_FPGA_AWAKE2 = PIN_FPGA_AWAKE + 1;
    public static final int PIN_ADC2V5_EN = PIN_FPGA_AWAKE2 + 1;
    public static final int PIN_POWER_ADC = PIN_ADC2V5_EN + 1;
    public static final int PIN_VERSION_0 = PIN_POWER_ADC + 1;
    public static final int PIN_VERSION_1 = PIN_VERSION_0 + 1;
    public static final int PIN_VERSION_2 = PIN_VERSION_1 + 1;
    public static final int PIN_VERSION_3 = PIN_VERSION_2 + 1;
    public static final int PIN_VERSION_4 = PIN_VERSION_3 + 1;
    public static final int PIN_VERSION_5 = PIN_VERSION_4 + 1;
    public static final int PIN_VERSION_6 = PIN_VERSION_5 + 1;
    public static final int PIN_VERSION_7 = PIN_VERSION_6 + 1;
    public static final int PIN_CLK_PLL_LE = PIN_VERSION_7 + 1;
    public static final int PIN_CLK_PLL_CE = PIN_CLK_PLL_LE + 1;
    public static final int FPGA_VCC1V0 = PIN_CLK_PLL_CE + 1;
    public static final int FPGA_VCC1V2 = FPGA_VCC1V0 + 1;
    public static final int FPGA_VCC1V8 = FPGA_VCC1V2 + 1;
    public static final int FPGA_VCC3V3 = FPGA_VCC1V8 + 1;

    public static final int FPGA_VCC3V3_EXT = FPGA_VCC3V3 + 1;
    public static final int PIN_POWER_PANEL = FPGA_VCC3V3_EXT + 1;
    public static final int PIN_POWER58_PANEL = PIN_POWER_PANEL + 1;
    public static final int PIN_PROBE_EN = PIN_POWER58_PANEL + 1;
    public static final int PIN_PROBE58_EN = PIN_PROBE_EN + 1;
    public static final int PIN_PROBE_IO = PIN_PROBE58_EN + 1;
    public static final int PIN_PROBE58_IO = PIN_PROBE_IO + 1;
    public static final int PIN_TRIG_CTL = PIN_PROBE58_IO + 1;
    public static final int PIN_10M_CLK_CTL = PIN_TRIG_CTL + 1;
    public static final int PIN_50V_PWR = PIN_10M_CLK_CTL + 1;
    private static final int PIN_MAX = PIN_50V_PWR + 1;


    private GpioDev mGpioDev[] = new GpioDev[PIN_MAX];

    private static volatile HwGpioManager instance = null;

    public static HwGpioManager getInstance(){
        return instance;
    }
    public static HwGpioManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SpiDevManager.class) {
                if (instance == null && context != null) {
                    instance = new HwGpioManager(context);
                }
            }
        }
        return instance;
    }
    private int hwVersion = 0;
    @SuppressLint("WrongConstant")
    private HwGpioManager(Context context){
        mContext = context;
        hwVersion = 0;
        mGpioManager = (GpioManager)mContext.getSystemService(HwServiceName.GPIO_SERVICE);

        init3588Gpio();

    };

    private void init3588Gpio(){
        mGpioDev[PIN_FPGA_nCONFIG] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D6),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_FPGA_CONFIG_DONE] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_B0),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_UP);
        mGpioDev[PIN_FPGA_nSTATUS] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D7),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_UP);
        mGpioDev[PIN_FPGA_nRST] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A3),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_POWER_ANALOG] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D0),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_POWER_ANALOG58] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D2),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);

        mGpioDev[PIN_CH_MODEL_OE] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B2),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_CH_MODEL_DS] =  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B6),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_CH_MODEL_SRCLR]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_B7),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_CH_MODEL_SRCLK]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_D1),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_CH_MODEL_RCLK]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO3_D2),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);


        mGpioDev[PIN_FPGA_AWAKE]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A4),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_FPGA_AWAKE2]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A5),GpioDev.GPIO_DIRECTION_IN,GpioDev.GPIO_PULL_DISABLE);
        //
        mGpioDev[PIN_POWER_PANEL]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D1),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_POWER58_PANEL]=  new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D3),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);


        mGpioDev[FPGA_VCC1V0] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_B6),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        if(HardwareProduct.isMHO28V1()){
            mGpioDev[FPGA_VCC1V2] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A7), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE);
        }else {
            mGpioDev[FPGA_VCC1V2] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_B7), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE);
        }
        mGpioDev[FPGA_VCC1V8] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_C0),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        if(HardwareProduct.isMHO28V1()){
            mGpioDev[FPGA_VCC3V3] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_A5),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        }else {
            mGpioDev[FPGA_VCC3V3] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO2_C1),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        }

        mGpioDev[FPGA_VCC3V3_EXT] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_B7),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);

        mGpioDev[PIN_ADC2V5_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D0), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE);


        mGpioDev[PIN_POWER_ADC] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D1), GpioDev.GPIO_DIRECTION_OUT_HIGH, GpioDev.GPIO_PULL_DISABLE);


        //
        mGpioDev[PIN_PROBE_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO4_D5), GpioDev.GPIO_DIRECTION_OUT_LOW, GpioDev.GPIO_PULL_DISABLE);

        if(!HardwareProduct.isMHO28V1()) {
            mGpioDev[PIN_PROBE58_EN] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_A7), GpioDev.GPIO_DIRECTION_OUT_LOW, GpioDev.GPIO_PULL_DISABLE);
        }

        //外部探头供电
        mGpioDev[PIN_PROBE_IO] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO0_A4),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_PROBE58_IO] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO0_B2),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);

        mGpioDev[PIN_TRIG_CTL] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_C4),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_10M_CLK_CTL] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_C6),GpioDev.GPIO_DIRECTION_OUT_LOW,GpioDev.GPIO_PULL_DISABLE);
        mGpioDev[PIN_50V_PWR] = new GpioDev(mGpioManager.openGpio(GpioDev.GPIO1_D5),GpioDev.GPIO_DIRECTION_OUT_HIGH,GpioDev.GPIO_PULL_DISABLE);

    }




    public GpioDev getGpioDev(@GPIO_PIN int pin){

        return mGpioDev[pin];
    }
    public int getHWVersion(){
        return hwVersion;
    }


    public void standby(){

    }
}
