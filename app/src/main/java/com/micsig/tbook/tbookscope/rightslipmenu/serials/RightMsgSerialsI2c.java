package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsI2c implements ISerialsDetails {
    private RightBeanSelect sda;
    private RightBeanSelect scl;

    public RightBeanSelect getSda() {
        return sda;
    }

    public void setSda(RightBeanSelect sda) {
        if (this.sda == null) {
            this.sda = sda;
        } else {
            this.sda = sda;
            setAllUnSelect();
            this.sda.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getScl() {
        return scl;
    }

    public void setScl(RightBeanSelect scl) {
        if (this.scl == null) {
            this.scl = scl;
        } else {
            this.scl = scl;
            setAllUnSelect();
            this.scl.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        this.sda.setRxMsgSelect(true);
        this.scl.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgSerialsI2c{" +
                "sda=" + sda +
                ", scl=" + scl +
                '}';
    }
}
