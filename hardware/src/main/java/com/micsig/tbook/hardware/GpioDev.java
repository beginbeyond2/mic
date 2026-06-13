package com.micsig.tbook.hardware;

import android.hardware.Gpio;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zhuzh on 2018/3/9.
 */

public class GpioDev {

    @IntDef({
            GPIO0_A0,
            GPIO0_A1,
            GPIO0_A2,
            GPIO0_A3,
            GPIO0_A4,
            GPIO0_A5,
            GPIO0_A6,
            GPIO0_A7,
            GPIO0_B0,
            GPIO0_B1,
            GPIO0_B2 ,
            GPIO0_B3 ,
            GPIO0_B4 ,
            GPIO0_B5 ,
            GPIO0_B6 ,
            GPIO0_B7 ,
            GPIO0_C0 ,
            GPIO0_C1 ,
            GPIO0_C2 ,
            GPIO0_C3 ,
            GPIO0_C4 ,
            GPIO0_C5 ,
            GPIO0_C6 ,
            GPIO0_C7 ,
            GPIO0_D0 ,
            GPIO0_D1 ,
            GPIO0_D2 ,
            GPIO0_D3 ,
            GPIO0_D4 ,
            GPIO0_D5 ,
            GPIO0_D6 ,
            GPIO0_D7 ,
            GPIO1_A0,
            GPIO1_A1,
            GPIO1_A2,
            GPIO1_A3,
            GPIO1_A4,
            GPIO1_A5,
            GPIO1_A6,
            GPIO1_A7,
            GPIO1_B0,
            GPIO1_B1,
            GPIO1_B2 ,
            GPIO1_B3 ,
            GPIO1_B4 ,
            GPIO1_B5 ,
            GPIO1_B6 ,
            GPIO1_B7 ,
            GPIO1_C0 ,
            GPIO1_C1 ,
            GPIO1_C2 ,
            GPIO1_C3 ,
            GPIO1_C4 ,
            GPIO1_C5 ,
            GPIO1_C6 ,
            GPIO1_C7 ,
            GPIO1_D0 ,
            GPIO1_D1 ,
            GPIO1_D2 ,
            GPIO1_D3 ,
            GPIO1_D4 ,
            GPIO1_D5 ,
            GPIO1_D6 ,
            GPIO1_D7 ,
            GPIO2_A0,
            GPIO2_A1,
            GPIO2_A2,
            GPIO2_A3,
            GPIO2_A4,
            GPIO2_A5,
            GPIO2_A6,
            GPIO2_A7,
            GPIO2_B0,
            GPIO2_B1,
            GPIO2_B2 ,
            GPIO2_B3 ,
            GPIO2_B4 ,
            GPIO2_B5 ,
            GPIO2_B6 ,
            GPIO2_B7 ,
            GPIO2_C0 ,
            GPIO2_C1 ,
            GPIO2_C2 ,
            GPIO2_C3 ,
            GPIO2_C4 ,
            GPIO2_C5 ,
            GPIO2_C6 ,
            GPIO2_C7 ,
            GPIO2_D0 ,
            GPIO2_D1 ,
            GPIO2_D2 ,
            GPIO2_D3 ,
            GPIO2_D4 ,
            GPIO2_D5 ,
            GPIO2_D6 ,
            GPIO2_D7 ,
            GPIO3_A0,
            GPIO3_A1,
            GPIO3_A2,
            GPIO3_A3,
            GPIO3_A4,
            GPIO3_A5,
            GPIO3_A6,
            GPIO3_A7,
            GPIO3_B0,
            GPIO3_B1,
            GPIO3_B2 ,
            GPIO3_B3 ,
            GPIO3_B4 ,
            GPIO3_B5 ,
            GPIO3_B6 ,
            GPIO3_B7 ,
            GPIO3_C0 ,
            GPIO3_C1 ,
            GPIO3_C2 ,
            GPIO3_C3 ,
            GPIO3_C4 ,
            GPIO3_C5 ,
            GPIO3_C6 ,
            GPIO3_C7 ,
            GPIO3_D0 ,
            GPIO3_D1 ,
            GPIO3_D2 ,
            GPIO3_D3 ,
            GPIO3_D4 ,
            GPIO3_D5 ,
            GPIO3_D6 ,
            GPIO3_D7 ,
            GPIO4_A0,
            GPIO4_A1,
            GPIO4_A2,
            GPIO4_A3,
            GPIO4_A4,
            GPIO4_A5,
            GPIO4_A6,
            GPIO4_A7,
            GPIO4_B0,
            GPIO4_B1,
            GPIO4_B2 ,
            GPIO4_B3 ,
            GPIO4_B4 ,
            GPIO4_B5 ,
            GPIO4_B6 ,
            GPIO4_B7 ,
            GPIO4_C0 ,
            GPIO4_C1 ,
            GPIO4_C2 ,
            GPIO4_C3 ,
            GPIO4_C4 ,
            GPIO4_C5 ,
            GPIO4_C6 ,
            GPIO4_C7 ,
            GPIO4_D0 ,
            GPIO4_D1 ,
            GPIO4_D2 ,
            GPIO4_D3 ,
            GPIO4_D4 ,
            GPIO4_D5 ,
            GPIO4_D6 ,
            GPIO4_D7 ,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIOIdx {}

    //0
    public static final int GPIO0_A0 = 0;
    public static final int GPIO0_A1 = 1;
    public static final int GPIO0_A2 = 2;
    public static final int GPIO0_A3 = 3;
    public static final int GPIO0_A4 = 4;
    public static final int GPIO0_A5 = 5;
    public static final int GPIO0_A6 = 6;
    public static final int GPIO0_A7 = 7;
    public static final int GPIO0_B0 = 8;
    public static final int GPIO0_B1 = 9;
    public static final int GPIO0_B2 = 10;
    public static final int GPIO0_B3 = 11;
    public static final int GPIO0_B4 = 12;
    public static final int GPIO0_B5 = 13;
    public static final int GPIO0_B6 = 14;
    public static final int GPIO0_B7 = 15;
    public static final int GPIO0_C0 = 16;
    public static final int GPIO0_C1 = 17;
    public static final int GPIO0_C2 = 18;
    public static final int GPIO0_C3 = 19;
    public static final int GPIO0_C4 = 20;
    public static final int GPIO0_C5 = 21;
    public static final int GPIO0_C6 = 22;
    public static final int GPIO0_C7 = 23;
    public static final int GPIO0_D0 = 24;
    public static final int GPIO0_D1 = 25;
    public static final int GPIO0_D2 = 26;
    public static final int GPIO0_D3 = 27;
    public static final int GPIO0_D4 = 28;
    public static final int GPIO0_D5 = 29;
    public static final int GPIO0_D6 = 30;
    public static final int GPIO0_D7 = 31;
    //1
    public static final int GPIO1_A0 = 32 + 0;
    public static final int GPIO1_A1 = 32 + 1;
    public static final int GPIO1_A2 = 32 + 2;
    public static final int GPIO1_A3 = 32 + 3;
    public static final int GPIO1_A4 = 32 + 4;
    public static final int GPIO1_A5 = 32 + 5;
    public static final int GPIO1_A6 = 32 + 6;
    public static final int GPIO1_A7 = 32 + 7;
    public static final int GPIO1_B0 = 32 + 8;
    public static final int GPIO1_B1 = 32 + 9;
    public static final int GPIO1_B2 = 32 + 10;
    public static final int GPIO1_B3 = 32 + 11;
    public static final int GPIO1_B4 = 32 + 12;
    public static final int GPIO1_B5 = 32 + 13;
    public static final int GPIO1_B6 = 32 + 14;
    public static final int GPIO1_B7 = 32 + 15;
    public static final int GPIO1_C0 = 32 + 16;
    public static final int GPIO1_C1 = 32 + 17;
    public static final int GPIO1_C2 = 32 + 18;
    public static final int GPIO1_C3 = 32 + 19;
    public static final int GPIO1_C4 = 32 + 20;
    public static final int GPIO1_C5 = 32 + 21;
    public static final int GPIO1_C6 = 32 + 22;
    public static final int GPIO1_C7 = 32 + 23;
    public static final int GPIO1_D0 = 32 + 24;
    public static final int GPIO1_D1 = 32 + 25;
    public static final int GPIO1_D2 = 32 + 26;
    public static final int GPIO1_D3 = 32 + 27;
    public static final int GPIO1_D4 = 32 + 28;
    public static final int GPIO1_D5 = 32 + 29;
    public static final int GPIO1_D6 = 32 + 30;
    public static final int GPIO1_D7 = 32 + 31;
    //2
    public static final int GPIO2_A0 = 64 + 0;
    public static final int GPIO2_A1 = 64 + 1;
    public static final int GPIO2_A2 = 64 + 2;
    public static final int GPIO2_A3 = 64 + 3;
    public static final int GPIO2_A4 = 64 + 4;
    public static final int GPIO2_A5 = 64 + 5;
    public static final int GPIO2_A6 = 64 + 6;
    public static final int GPIO2_A7 = 64 + 7;
    public static final int GPIO2_B0 = 64 + 8;
    public static final int GPIO2_B1 = 64 + 9;
    public static final int GPIO2_B2 = 64 + 10;
    public static final int GPIO2_B3 = 64 + 11;
    public static final int GPIO2_B4 = 64 + 12;
    public static final int GPIO2_B5 = 64 + 13;
    public static final int GPIO2_B6 = 64 + 14;
    public static final int GPIO2_B7 = 64 + 15;
    public static final int GPIO2_C0 = 64 + 16;
    public static final int GPIO2_C1 = 64 + 17;
    public static final int GPIO2_C2 = 64 + 18;
    public static final int GPIO2_C3 = 64 + 19;
    public static final int GPIO2_C4 = 64 + 20;
    public static final int GPIO2_C5 = 64 + 21;
    public static final int GPIO2_C6 = 64 + 22;
    public static final int GPIO2_C7 = 64 + 23;
    public static final int GPIO2_D0 = 64 + 24;
    public static final int GPIO2_D1 = 64 + 25;
    public static final int GPIO2_D2 = 64 + 26;
    public static final int GPIO2_D3 = 64 + 27;
    public static final int GPIO2_D4 = 64 + 28;
    public static final int GPIO2_D5 = 64 + 29;
    public static final int GPIO2_D6 = 64 + 30;
    public static final int GPIO2_D7 = 64 + 31;
    //3
    public static final int GPIO3_A0 = 96 + 0;
    public static final int GPIO3_A1 = 96 + 1;
    public static final int GPIO3_A2 = 96 + 2;
    public static final int GPIO3_A3 = 96 + 3;
    public static final int GPIO3_A4 = 96 + 4;
    public static final int GPIO3_A5 = 96 + 5;
    public static final int GPIO3_A6 = 96 + 6;
    public static final int GPIO3_A7 = 96 + 7;
    public static final int GPIO3_B0 = 96 + 8;
    public static final int GPIO3_B1 = 96 + 9;
    public static final int GPIO3_B2 = 96 + 10;
    public static final int GPIO3_B3 = 96 + 11;
    public static final int GPIO3_B4 = 96 + 12;
    public static final int GPIO3_B5 = 96 + 13;
    public static final int GPIO3_B6 = 96 + 14;
    public static final int GPIO3_B7 = 96 + 15;
    public static final int GPIO3_C0 = 96 + 16;
    public static final int GPIO3_C1 = 96 + 17;
    public static final int GPIO3_C2 = 96 + 18;
    public static final int GPIO3_C3 = 96 + 19;
    public static final int GPIO3_C4 = 96 + 20;
    public static final int GPIO3_C5 = 96 + 21;
    public static final int GPIO3_C6 = 96 + 22;
    public static final int GPIO3_C7 = 96 + 23;
    public static final int GPIO3_D0 = 96 + 24;
    public static final int GPIO3_D1 = 96 + 25;
    public static final int GPIO3_D2 = 96 + 26;
    public static final int GPIO3_D3 = 96 + 27;
    public static final int GPIO3_D4 = 96 + 28;
    public static final int GPIO3_D5 = 96 + 29;
    public static final int GPIO3_D6 = 96 + 30;
    public static final int GPIO3_D7 = 96 + 31;
    //4
    public static final int GPIO4_A0 = 128 + 0;
    public static final int GPIO4_A1 = 128 + 1;
    public static final int GPIO4_A2 = 128 + 2;
    public static final int GPIO4_A3 = 128 + 3;
    public static final int GPIO4_A4 = 128 + 4;
    public static final int GPIO4_A5 = 128 + 5;
    public static final int GPIO4_A6 = 128 + 6;
    public static final int GPIO4_A7 = 128 + 7;
    public static final int GPIO4_B0 = 128 + 8;
    public static final int GPIO4_B1 = 128 + 9;
    public static final int GPIO4_B2 = 128 + 10;
    public static final int GPIO4_B3 = 128 + 11;
    public static final int GPIO4_B4 = 128 + 12;
    public static final int GPIO4_B5 = 128 + 13;
    public static final int GPIO4_B6 = 128 + 14;
    public static final int GPIO4_B7 = 128 + 15;
    public static final int GPIO4_C0 = 128 + 16;
    public static final int GPIO4_C1 = 128 + 17;
    public static final int GPIO4_C2 = 128 + 18;
    public static final int GPIO4_C3 = 128 + 19;
    public static final int GPIO4_C4 = 128 + 20;
    public static final int GPIO4_C5 = 128 + 21;
    public static final int GPIO4_C6 = 128 + 22;
    public static final int GPIO4_C7 = 128 + 23;
    public static final int GPIO4_D0 = 128 + 24;
    public static final int GPIO4_D1 = 128 + 25;
    public static final int GPIO4_D2 = 128 + 26;
    public static final int GPIO4_D3 = 128 + 27;
    public static final int GPIO4_D4 = 128 + 28;
    public static final int GPIO4_D5 = 128 + 29;
    public static final int GPIO4_D6 = 128 + 30;
    public static final int GPIO4_D7 = 128 + 31;


    @IntDef({GPIO_VAL_LOW,GPIO_VAL_HIGH})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIO_VAL {}

    //val
    public static final int GPIO_VAL_LOW = 0;
    public static final int GPIO_VAL_HIGH = 1;

    @IntDef({GPIO_PULL_DISABLE,GPIO_PULL_DOWN,GPIO_PULL_UP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIO_PULL {}
    //pull
    public static final int GPIO_PULL_DISABLE = 0;
    public static final int GPIO_PULL_DOWN = 1;
    public static final int GPIO_PULL_UP = 2;

    @IntDef({GPIO_DIRECTION_OUT_LOW,GPIO_DIRECTION_OUT_HIGH,GPIO_DIRECTION_IN})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIO_DIRECTION {}
    //dir
    public static final int GPIO_DIRECTION_OUT_LOW = 0;
    public static final int GPIO_DIRECTION_OUT_HIGH = 1;
    public static final int GPIO_DIRECTION_IN = 2;

    @IntDef({GPIO_DIRVE_0,GPIO_DIRVE_1,GPIO_DIRVE_2,GPIO_DIRVE_3})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GPIO_DIRVE {}
    //drv
    public static final int GPIO_DIRVE_0 = 0;
    public static final int GPIO_DIRVE_1 = 1;
    public static final int GPIO_DIRVE_2 = 2;
    public static final int GPIO_DIRVE_3 = 3;

    private Gpio mGpio = null;
    public GpioDev(Gpio gpio, @GPIO_DIRECTION int dir, @GPIO_PULL int pull){
        mGpio = gpio;
        mGpio.setPull(pull);
        mGpio.setDirection(dir);
    }

    public int getVal(){
        return mGpio.getValue();
    }

    public void setVal(@GPIO_VAL int val){
        mGpio.setValue(val);
    }


}
