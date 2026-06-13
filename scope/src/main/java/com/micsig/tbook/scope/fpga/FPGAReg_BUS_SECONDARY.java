package com.micsig.tbook.scope.fpga;

/**
 * Created by zhuzh on 2018-5-30.
 */

public class FPGAReg_BUS_SECONDARY extends FPGAReg {
    public FPGAReg_BUS_SECONDARY() {
        super(FPGA_BUS_SECONDARY, 4);
    }
    public void setLevel(int busIdx,int val){
        setVal(busIdx*8,8,val);
    }

}
