package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerPulsewidth implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel polar;
    private TopBeanChannel condition;
    private RxStringWithSelect pulsewidth;
    private RxStringWithSelect timeHighDetail;
    private RxStringWithSelect timeLowDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerPulsewidth topMsgTriggerPulsewidth = (TopMsgTriggerPulsewidth) super.clone();
        topMsgTriggerPulsewidth.triggerSource = (TopBeanChannel) topMsgTriggerPulsewidth.triggerSource.clone();
        topMsgTriggerPulsewidth.polar = (TopBeanChannel) topMsgTriggerPulsewidth.polar.clone();
        topMsgTriggerPulsewidth.condition = (TopBeanChannel) topMsgTriggerPulsewidth.condition.clone();
        topMsgTriggerPulsewidth.pulsewidth = (RxStringWithSelect) topMsgTriggerPulsewidth.pulsewidth.clone();
        topMsgTriggerPulsewidth.timeHighDetail = (RxStringWithSelect) topMsgTriggerPulsewidth.timeHighDetail.clone();
        topMsgTriggerPulsewidth.timeLowDetail = (RxStringWithSelect) topMsgTriggerPulsewidth.timeLowDetail.clone();
        return topMsgTriggerPulsewidth;
    }

    public TopBeanChannel getTriggerSource() {
        return triggerSource;
    }

    public void setTriggerSource(TopBeanChannel triggerSource) {
        if (this.triggerSource == null) {
            this.triggerSource = triggerSource;
        } else {
            this.triggerSource = triggerSource;
            setAllUnSelect();
            this.triggerSource.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getPolar() {
        return polar;
    }

    public void setPolar(TopBeanChannel polar) {
        if (this.polar == null) {
            this.polar = polar;
        } else {
            this.polar = polar;
            setAllUnSelect();
            this.polar.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCondition() {
        return condition;
    }

    public void setCondition(TopBeanChannel condition) {
        if (this.condition == null) {
            this.condition = condition;
        } else {
            this.condition = condition;
            setAllUnSelect();
            this.condition.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getPulsewidth() {
        return pulsewidth;
    }

    public void setPulsewidth(String pulsewidth) {
        if (this.pulsewidth == null) {
            this.pulsewidth = new RxStringWithSelect(pulsewidth);
        } else {
            this.pulsewidth.setValue(pulsewidth);
            setAllUnSelect();
            this.pulsewidth.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getTimeHighDetail() {
        return timeHighDetail;
    }

    public void setTimeHighDetail(String timeHighDetail) {
        if (this.timeHighDetail == null) {
            this.timeHighDetail = new RxStringWithSelect(timeHighDetail);
        } else {
            this.timeHighDetail.setValue(timeHighDetail);
            setAllUnSelect();
            this.timeHighDetail.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getTimeLowDetail() {
        return timeLowDetail;
    }

    public void setTimeLowDetail(String timeLowDetail) {
        if (this.timeLowDetail == null) {
            this.timeLowDetail = new RxStringWithSelect(timeLowDetail);
        } else {
            this.timeLowDetail.setValue(timeLowDetail);
            setAllUnSelect();
            this.timeLowDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        polar.setRxMsgSelect(false);
        condition.setRxMsgSelect(false);
        pulsewidth.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerPulsewidth{" +
                "triggerSource=" + triggerSource +
                ", polar=" + polar +
                ", condition=" + condition +
                ", pulsewidth='" + pulsewidth +
                ", timeHighDetail='" + timeHighDetail +
                ", timeLowDetail='" + timeLowDetail + '\'' +
                '}';
    }
}
