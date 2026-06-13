package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsUart implements ISerialsDetails,IDigits {
    private RightBeanSelect rx;
    private RightBeanSelect idleLevel;
    private RightBeanSelect check;
    private RightBeanSelect bits;
    private RightBeanSelect baudRate;
    private RightBeanSelect display;
    private RxStringWithSelect baudRateDefine;//baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

    public RightBeanSelect getRx() {
        return rx;
    }

    public void setRx(RightBeanSelect rx) {
        if (this.rx == null) {
            this.rx = rx;
        } else {
            this.rx = rx;
            setAllUnSelect();
            this.rx.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getIdleLevel() {
        return idleLevel;
    }

    public void setIdleLevel(RightBeanSelect idleLevel) {
        if (this.idleLevel == null) {
            this.idleLevel = idleLevel;
        } else {
            this.idleLevel = idleLevel;
            setAllUnSelect();
            this.idleLevel.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getCheck() {
        return check;
    }

    public void setCheck(RightBeanSelect check) {
        if (this.check == null) {
            this.check = check;
        } else {
            this.check = check;
            setAllUnSelect();
            this.check.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getBits() {
        return bits;
    }

    public void setBits(RightBeanSelect bits) {
        if (this.bits == null) {
            this.bits = bits;
        } else {
            this.bits = bits;
            setAllUnSelect();
            this.bits.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(RightBeanSelect baudRate) {
        if (this.baudRate == null) {
            this.baudRate = baudRate;
        } else {
            this.baudRate = baudRate;
            setAllUnSelect();
            this.baudRate.setRxMsgSelect(true);
        }
    }


    public RxStringWithSelect getBaudRateDefine() {
        return baudRateDefine;
    }

    public void setBaudRateDefine(String baudRateDefine) {
        if (this.baudRateDefine == null) {
            this.baudRateDefine = new RxStringWithSelect(baudRateDefine);
        } else {
            this.baudRateDefine.setValue(baudRateDefine);
            setAllUnSelect();
            this.baudRateDefine.setRxMsgSelect(true);
        }
    }


    public RightBeanSelect getDisplay() {
        return display;
    }

    public void setDisplay(RightBeanSelect display) {
        if (this.display == null) {
            this.display = display;
        } else {
            this.display = display;
            setAllUnSelect();
            this.display.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        rx.setRxMsgSelect(false);
        idleLevel.setRxMsgSelect(false);
        check.setRxMsgSelect(false);
        bits.setRxMsgSelect(false);
        baudRate.setRxMsgSelect(false);
        display.setRxMsgSelect(false);
        baudRateDefine.setRxMsgSelect(false);
    }

    public int getIntBits() {
        return Integer.parseInt(bits.getText().replace("bit", ""));
    }

    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsUartDisplay);
        if (display.getText().equals(ss[0])) {
            return DIGITS_16;
        } else if (display.getText().equals(ss[1])) {
            return DIGITS_2;
        } else {
            return DIGITS_16;
        }
    }

    @Override
    public String toString() {
        return "RightMsgSerialsUart{" +
                "rx=" + rx +
                ", idleLevel=" + idleLevel +
                ", check=" + check +
                ", bits=" + bits +
                ", baudRate='" + baudRate + '\'' +
                ", display=" + display +
                '}';
    }
}
