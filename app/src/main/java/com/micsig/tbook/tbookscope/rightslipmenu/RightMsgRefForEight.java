package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/5/12.
 */

public class RightMsgRefForEight {
    private RxBooleanWithSelect refChecked;
    private RxStringWithSelect label;

    private int refChannelNumber = TChan.R1;//默认R1

    private boolean isUpClick;

    public boolean isUpClick() {
        return isUpClick;
    }

    public void setUpClick(boolean upClick) {
        isUpClick = upClick;
    }

    public int getRefChannelNumber() {
        return refChannelNumber;
    }

    public void setRefChannelNumber(int refChannelNumber) {
        this.refChannelNumber = refChannelNumber;
    }

    public RxBooleanWithSelect getRefChecked() {
        return refChecked;
    }

    public void setRefChecked(boolean refChecked) {
        if (this.refChecked == null) {
            this.refChecked = new RxBooleanWithSelect(refChecked);
        } else {
            this.refChecked.setValue(refChecked);
            setAllUnSelect();
            this.refChecked.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (this.label == null) {
            this.label = new RxStringWithSelect(label);
        } else {
            this.label.setValue(label);
            setAllUnSelect();
            this.label.setRxMsgSelect(true);
        }
    }


    private void setAllUnSelect() {
        refChecked.setRxMsgSelect(false);
        label.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgRef{" +
                "refChannelNumber=" + refChannelNumber +
                ", refChecked=" + refChecked +
                ", refLabel=" + label +
                '}';
    }
}
