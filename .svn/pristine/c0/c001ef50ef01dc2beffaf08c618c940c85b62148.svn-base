package com.micsig.tbook.hardware;

import android.os.Build;

public class HardwareProduct {

    public static final String RK3588_MHO38_V1 = "rk3588_MHO38";
    public static final String RK3588_MHO28_V1 = "rk3588_MHO28";
    public static final String RK3588_MHO68_V1 = "rk3588_MHO";
    public static final String RK3588_MHO68_V2 = "rk3588_MHO2";



    public static boolean isMHO68V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO68_V1);
    }
    public static boolean isMHO68V2() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO68_V2);
    }
    public static boolean isMHO38V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO38_V1);
    }
    public static boolean isMHO28V1() {
        return Build.PRODUCT.equalsIgnoreCase(RK3588_MHO28_V1);
    }
    public static boolean isBattery(){
        return false;
    }
    public static boolean isExtKeyboard(){
        return true;
    }
    public static boolean isFSpiBoot(){

        return !HardwareProduct.isMHO28V1();
    }

    public static boolean isFanSpeed(){
        return true;
    }

}
