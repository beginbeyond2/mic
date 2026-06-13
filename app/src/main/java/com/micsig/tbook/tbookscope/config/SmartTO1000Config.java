package com.micsig.tbook.tbookscope.config;

import android.os.Build;

import com.micsig.tbook.hardware.Hardware;
import com.micsig.tbook.hardware.HardwareProduct;
import com.micsig.tbook.scope.Sample.MemDepthFactory;
import com.micsig.tbook.scope.horizontal.HorizontalAxis;
import com.micsig.tbook.scope.vertical.VerticalAxis;

/**
 * Created by zhuzh on 2018-12-5.
 */

public class SmartTO1000Config extends BaseConfig {

    public SmartTO1000Config(){
        this(MemDepthFactory.getDefaultMemDepth());
    }
    public SmartTO1000Config(int mem){
        super();
        if(mem < getMemDepth()){
            setMemDepth(mem);
        }
    }

    @Override
    protected int getMinVerticalGear() {
        return VerticalAxis.DANG_1mV;
    }

    @Override
    protected int getMaxVerticalGear() {
        return HardwareProduct.isMHO68V1() ? VerticalAxis.DANG_5V : VerticalAxis.DANG_10V;
    }

    @Override
    protected int getMinHorizontalGear() {
        return HorizontalAxis.TSI_1KS;
    }

    @Override
    protected int getMaxHorizontalGear() {
        switch(Build.PRODUCT){
            case HardwareProduct.RK3588_MHO38_V1:
            case HardwareProduct.RK3588_MHO28_V1:
                return HorizontalAxis.TSI_1nS;
        }
        return HorizontalAxis.TSI_250pS;
    }


    @Override
    public boolean isValidProduct() {
        return Build.PRODUCT.startsWith("rk3588_MHO");
    }
}
