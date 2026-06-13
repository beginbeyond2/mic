package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsSpi implements ISerialsDetails {
    private RightBeanSelect clk;
    private RightBeanSelect data;
    private RightBeanSelect cs;
    private RightBeanSelect bit;
    private RxBooleanWithSelect clkLow;
    private RxBooleanWithSelect dataLow;
    private RxBooleanWithSelect csLow;
    private RxBooleanWithSelect csSwitch;

    public RightBeanSelect getClk() {
        return clk;
    }

    public void setClk(RightBeanSelect clk) {
        if (this.clk == null) {
            this.clk = clk;
        } else {
            this.clk = clk;
            setAllUnSelect();
            this.clk.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getData() {
        return data;
    }

    public void setData(RightBeanSelect data) {
        if (this.data == null) {
            this.data = data;
        } else {
            this.data = data;
            setAllUnSelect();
            this.data.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getCs() {
        return cs;
    }

    public void setCs(RightBeanSelect cs) {
        if (this.cs == null) {
            this.cs = cs;
        } else {
            this.cs = cs;
            setAllUnSelect();
            this.cs.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getBit() {
        return bit;
    }

    public void setBit(RightBeanSelect bit) {
        if (this.bit == null) {
            this.bit = bit;
        } else {
            this.bit = bit;
            setAllUnSelect();
            this.bit.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getClkLow() {
        return clkLow;
    }

    public void setClkLow(boolean low) {
        if (this.clkLow == null) {
            this.clkLow = new RxBooleanWithSelect(low);
        } else {
            this.clkLow.setValue(low);
            setAllUnSelect();
            this.clkLow.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getDataLow() {
        return dataLow;
    }

    public void setDataLow(boolean low) {
        if (this.dataLow == null) {
            this.dataLow = new RxBooleanWithSelect(low);
        } else {
            this.dataLow.setValue(low);
            setAllUnSelect();
            this.dataLow.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getCsLow() {
        return csLow;
    }

    public void setCsLow(boolean low) {
        if (this.csLow == null) {
            this.csLow = new RxBooleanWithSelect(low);
        } else {
            this.csLow.setValue(low);
            setAllUnSelect();
            this.csLow.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getCsSwitch() {
        return csSwitch;
    }

    public void setCsSwitch(boolean csSwitch) {
        if (this.csSwitch == null) {
            this.csSwitch = new RxBooleanWithSelect(csSwitch);
        } else {
            this.csSwitch.setValue(csSwitch);
            setAllUnSelect();
            this.csSwitch.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        clk.setRxMsgSelect(false);
        data.setRxMsgSelect(false);
        cs.setRxMsgSelect(false);
        bit.setRxMsgSelect(false);
        clkLow.setRxMsgSelect(false);
        dataLow.setRxMsgSelect(false);
        csLow.setRxMsgSelect(false);
        csSwitch.setRxMsgSelect(false);
    }

    public int getIntBit() {
        return Integer.parseInt(bit.getText().replace("bit", ""));
    }

    @Override
    public String toString() {
        return "RightMsgSerialsSpi{" +
                "clk=" + clk +
                ", data=" + data +
                ", cs=" + cs +
                ", bit=" + bit +
                ", clkLow='" + clkLow + '\'' +
                ", dataLow='" + dataLow + '\'' +
                ", csLow='" + csLow + '\'' +
                ", csSwitch=" + csSwitch +
                '}';
    }
}
