package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;

/**
 * Created by yangj on 2017/5/12.
 */

public class RightMsgRef {
    private RxBooleanWithSelect refChecked;
    private RxBooleanWithSelect r1Checked;
    private RxStringWithSelect r1Recall;
    private RxBooleanWithSelect r2Checked;
    private RxStringWithSelect r2Recall;
    private RxBooleanWithSelect r3Checked;
    private RxStringWithSelect r3Recall;
    private RxBooleanWithSelect r4Checked;
    private RxStringWithSelect r4Recall;
    private int refChannelNumber = 1;

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

    public RxBooleanWithSelect getR1Checked() {
        return r1Checked;
    }

    public void setR1Checked(boolean r1Checked) {
        if (this.r1Checked == null) {
            this.r1Checked = new RxBooleanWithSelect(r1Checked);
        } else {
            this.r1Checked.setValue(r1Checked);
            setAllUnSelect();
            this.r1Checked.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getR2Checked() {
        return r2Checked;
    }

    public void setR2Checked(boolean r2Checked) {
        if (this.r2Checked == null) {
            this.r2Checked = new RxBooleanWithSelect(r2Checked);
        } else {
            this.r2Checked.setValue(r2Checked);
            setAllUnSelect();
            this.r2Checked.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getR3Checked() {
        return r3Checked;
    }

    public void setR3Checked(boolean r3Checked) {
        if (this.r3Checked == null) {
            this.r3Checked = new RxBooleanWithSelect(r3Checked);
        } else {
            this.r3Checked.setValue(r3Checked);
            setAllUnSelect();
            this.r3Checked.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getR4Checked() {
        return r4Checked;
    }

    public void setR4Checked(boolean r4Checked) {
        if (this.r4Checked == null) {
            this.r4Checked = new RxBooleanWithSelect(r4Checked);
        } else {
            this.r4Checked.setValue(r4Checked);
            setAllUnSelect();
            this.r4Checked.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getR1Recall() {
        return r1Recall;
    }

    public void setR1Recall(String r1Recall) {
        if (this.r1Recall == null) {
            this.r1Recall = new RxStringWithSelect(r1Recall);
        } else {
            this.r1Recall.setValue(r1Recall);
            setAllUnSelect();
            this.r1Recall.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getR2Recall() {
        return r2Recall;
    }

    public void setR2Recall(String r2Recall) {
        if (this.r2Recall == null) {
            this.r2Recall = new RxStringWithSelect(r2Recall);
        } else {
            this.r2Recall.setValue(r2Recall);
            setAllUnSelect();
            this.r2Recall.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getR3Recall() {
        return r3Recall;
    }

    public void setR3Recall(String r3Recall) {
        if (this.r3Recall == null) {
            this.r3Recall = new RxStringWithSelect(r3Recall);
        } else {
            this.r3Recall.setValue(r3Recall);
            setAllUnSelect();
            this.r3Recall.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getR4Recall() {
        return r4Recall;
    }

    public void setR4Recall(String r4Recall) {
        if (this.r4Recall == null) {
            this.r4Recall = new RxStringWithSelect(r4Recall);
        } else {
            this.r4Recall.setValue(r4Recall);
            setAllUnSelect();
            this.r4Recall.setRxMsgSelect(true);
        }
    }

    public boolean isAllCheckEquals(boolean r1, boolean r2, boolean r3, boolean r4) {
        return r1 == r1Checked.isValue() && r2 == r2Checked.isValue() && r3 == r3Checked.isValue() && r4 == r4Checked.isValue();
    }

    private void setAllUnSelect() {
        r1Checked.setRxMsgSelect(false);
        r2Checked.setRxMsgSelect(false);
        r3Checked.setRxMsgSelect(false);
        r4Checked.setRxMsgSelect(false);
        r1Recall.setRxMsgSelect(false);
        r2Recall.setRxMsgSelect(false);
        r3Recall.setRxMsgSelect(false);
        r4Recall.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgRef{" +
                "r1Checked=" + r1Checked +
                ", r1Recall='" + r1Recall + '\'' +
                ", r2Checked=" + r2Checked +
                ", r2Recall='" + r2Recall + '\'' +
                ", r3Checked=" + r3Checked +
                ", r3Recall='" + r3Recall + '\'' +
                ", r4Checked=" + r4Checked +
                ", r4Recall='" + r4Recall + '\'' +
                '}';
    }
}
