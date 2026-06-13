package com.micsig.tbook.scope.fpga;

/**
 * Created by zhuzh on 2018-5-30.
 */

public class FPGAReg_BUS_PRIMARY_EXT extends FPGAReg {
    public FPGAReg_BUS_PRIMARY_EXT() {
        super(FPGA_BUS_PRIMARY_EXT, 4);
    }

    public void setLevel(int chIdx,int val){
        setVal(chIdx*8,8,val);
    }
}
