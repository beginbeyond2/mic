package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayCommon implements IDisplayDetail {
    private TopBeanChannel horRef;
    private TopBeanChannel timeBase;
    private RxBooleanWithSelect roll;
    private RxBooleanWithSelect cct;

    private float alpha = 0.85f;

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = (1  -  alpha * 0.5f);
    }

    public TopBeanChannel getHorRef() {
        return horRef;
    }

    public void setHorRef(TopBeanChannel horRef) {
        if (this.horRef == null) {
            this.horRef = horRef;
        } else {
            this.horRef = horRef;
            setAllUnSelect();
            this.horRef.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(TopBeanChannel timeBase) {
        if (this.timeBase == null) {
            this.timeBase = timeBase;
        } else {
            this.timeBase = timeBase;
            setAllUnSelect();
            this.timeBase.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRoll() {
        return roll;
    }

    public void setRoll(boolean roll) {
        if (this.roll == null) {
            this.roll = new RxBooleanWithSelect(roll);
        } else {
            this.roll.setValue(roll);
            setAllUnSelect();
            this.roll.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getCct() {
        return cct;
    }

    public void setCct(boolean cct) {
        if (this.cct == null) {
            this.cct = new RxBooleanWithSelect(cct);
        } else {
            this.cct.setValue(cct);
            setAllUnSelect();
            this.cct.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        horRef.setRxMsgSelect(false);
        timeBase.setRxMsgSelect(false);
        roll.setRxMsgSelect(false);
        cct.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgDisplayCommon{" +
                "horRef=" + horRef +
                ", timeBase=" + timeBase +
                ", roll=" + roll +
                ", cct=" + cct +
                '}';
    }
}
