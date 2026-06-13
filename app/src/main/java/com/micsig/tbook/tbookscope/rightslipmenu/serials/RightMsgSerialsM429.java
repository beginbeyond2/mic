package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsM429 implements ISerialsDetails,IDigits {
    private RightBeanSelect source;
    private RightBeanSelect format;
    private RightBeanSelect display;
    private RightBeanSelect baudRate;

    public RightBeanSelect getSource() {
        return source;
    }

    public void setSource(RightBeanSelect source) {
        if (this.source == null) {
            this.source = source;
        } else {
            this.source = source;
            setAllUnSelect();
            this.source.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getFormat() {
        return format;
    }

    public void setFormat(RightBeanSelect format) {
        if (this.format == null) {
            this.format = format;
        } else {
            this.format = format;
            setAllUnSelect();
            this.format.setRxMsgSelect(true);
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

    private void setAllUnSelect() {
        source.setRxMsgSelect(false);
        format.setRxMsgSelect(false);
        display.setRxMsgSelect(false);
        baudRate.setRxMsgSelect(false);
    }

    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsM429Display);
        if (display.getText().equals(ss[0])) {
            return DIGITS_2;
        } else if (display.getText().equals(ss[1])) {
            return DIGITS_16;
        } else {
            return DIGITS_16;
        }
    }

    @Override
    public String toString() {
        return "RightMsgSerialsM429{" +
                "source=" + source +
                ", format=" + format +
                ", display=" + display +
                ", baudRate=" + baudRate +
                '}';
    }

    public String getFormatSimple() {
        switch (format.getIndex()) {
            case 0:
                return "LD";
            case 1:
                return "LDS";
            case 2:
                return "LSDS";
            default:
                return "";
        }
    }
}
