package com.micsig.tbook.scope.fpga;

/**
 * Created by zhuzh on 2018-5-30.
 */

public class FPGAReg_BUS_PRIMARY extends FPGAReg {
    public FPGAReg_BUS_PRIMARY() {
        super(FPGA_BUS_PRIMARY, 4);
    }

    public void setLevel(int chIdx,int val){
        setVal(chIdx*8,8,val);
    }
}
