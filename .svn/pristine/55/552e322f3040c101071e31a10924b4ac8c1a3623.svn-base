package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerSlope implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel edge;
    private TopBeanChannel condition;
    private RxStringWithSelect slopeTimeHighDetail;
    private RxStringWithSelect slopeTimeLowDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerSlope topMsgTriggerSlope = (TopMsgTriggerSlope) super.clone();
        topMsgTriggerSlope.triggerSource = (TopBeanChannel) topMsgTriggerSlope.triggerSource.clone();
        topMsgTriggerSlope.edge = (TopBeanChannel) topMsgTriggerSlope.edge.clone();
        topMsgTriggerSlope.condition = (TopBeanChannel) topMsgTriggerSlope.condition.clone();
        topMsgTriggerSlope.slopeTimeHighDetail = (RxStringWithSelect) topMsgTriggerSlope.slopeTimeHighDetail.clone();
        topMsgTriggerSlope.slopeTimeLowDetail = (RxStringWithSelect) topMsgTriggerSlope.slopeTimeLowDetail.clone();
        return topMsgTriggerSlope;
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

    public TopBeanChannel getEdge() {
        return edge;
    }

    public void setEdge(TopBeanChannel edge) {
        if (this.edge == null) {
            this.edge = edge;
        } else {
            this.edge = edge;
            setAllUnSelect();
            this.edge.setRxMsgSelect(true);
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

    public RxStringWithSelect getSlopeTimeHighDetail() {
        return slopeTimeHighDetail;
    }

    public void setSlopeTimeHighDetail(String slopeTimeHighDetail) {
        if (this.slopeTimeHighDetail == null) {
            this.slopeTimeHighDetail = new RxStringWithSelect(slopeTimeHighDetail);
        } else {
            this.slopeTimeHighDetail.setValue(slopeTimeHighDetail);
            setAllUnSelect();
            this.slopeTimeHighDetail.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getSlopeTimeLowDetail() {
        return slopeTimeLowDetail;
    }

    public void setSlopeTimeLowDetail(String slopeTimeLowDetail) {
        if (this.slopeTimeLowDetail == null) {
            this.slopeTimeLowDetail = new RxStringWithSelect(slopeTimeLowDetail);
        } else {
            this.slopeTimeLowDetail.setValue(slopeTimeLowDetail);
            setAllUnSelect();
            this.slopeTimeLowDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        edge.setRxMsgSelect(false);
        condition.setRxMsgSelect(false);
        slopeTimeHighDetail.setRxMsgSelect(false);
        slopeTimeLowDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerSlope{" +
                "triggerSource=" + triggerSource +
                ", edge=" + edge +
                ", condition=" + condition +
                ", slopeTimeHighDetail='" + slopeTimeHighDetail + '\'' +
                ", slopeTimeLowDetail='" + slopeTimeLowDetail + '\'' +
                '}';
    }
}
