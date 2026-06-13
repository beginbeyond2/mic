package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerRunt implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel polar;
    private TopBeanChannel condition;
    private RxStringWithSelect timeHighDetail;
    private RxStringWithSelect timeLowDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerRunt topMsgTriggerRunt = (TopMsgTriggerRunt) super.clone();
        topMsgTriggerRunt.triggerSource = (TopBeanChannel) topMsgTriggerRunt.triggerSource.clone();
        topMsgTriggerRunt.polar = (TopBeanChannel) topMsgTriggerRunt.polar.clone();
        topMsgTriggerRunt.condition = (TopBeanChannel) topMsgTriggerRunt.condition.clone();
        topMsgTriggerRunt.timeHighDetail = (RxStringWithSelect) topMsgTriggerRunt.timeHighDetail.clone();
        topMsgTriggerRunt.timeLowDetail = (RxStringWithSelect) topMsgTriggerRunt.timeLowDetail.clone();
        return topMsgTriggerRunt;
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
        timeHighDetail.setRxMsgSelect(false);
        timeLowDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerRunt{" +
                "triggerSource=" + triggerSource +
                ", polar=" + polar +
                ", condition=" + condition +
                ", timeHighDetail='" + timeHighDetail + '\'' +
                ", timeLowDetail='" + timeLowDetail + '\'' +
                '}';
    }
}
