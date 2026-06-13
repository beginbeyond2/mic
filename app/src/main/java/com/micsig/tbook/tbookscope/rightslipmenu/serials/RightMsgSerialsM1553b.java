package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import android.content.Context;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsM1553b implements ISerialsDetails,IDigits {
    private RightBeanSelect source;
    private RightBeanSelect display;

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
        source.setRxMsgSelect(false);
        display.setRxMsgSelect(false);
    }

    public int getIntDigits(Context context) {
        String[] ss = context.getResources().getStringArray(R.array.serialsM1553bDisplay);
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
        return "RightMsgSerialsM1553b{" +
                "source=" + source +
                ", display=" + display +
                '}';
    }
}
