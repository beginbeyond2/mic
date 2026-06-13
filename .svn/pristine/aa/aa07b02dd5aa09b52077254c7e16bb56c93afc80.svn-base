package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerEdge implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel triggerEdge;
    private TopBeanChannel triggerCouple;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerEdge topMsgTriggerEdge = (TopMsgTriggerEdge) super.clone();
        topMsgTriggerEdge.triggerSource = (TopBeanChannel) topMsgTriggerEdge.triggerSource.clone();
        topMsgTriggerEdge.triggerEdge = (TopBeanChannel) topMsgTriggerEdge.triggerEdge.clone();
        topMsgTriggerEdge.triggerCouple = (TopBeanChannel) topMsgTriggerEdge.triggerCouple.clone();
        return topMsgTriggerEdge;
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

    public TopBeanChannel getTriggerEdge() {
        return triggerEdge;
    }

    public void setTriggerEdge(TopBeanChannel triggerEdge) {
        if (this.triggerEdge == null) {
            this.triggerEdge = triggerEdge;
        } else {
            this.triggerEdge = triggerEdge;
            setAllUnSelect();
            this.triggerEdge.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getTriggerCouple() {
        return triggerCouple;
    }

    public void setTriggerCouple(TopBeanChannel triggerCouple) {
        if (this.triggerCouple == null) {
            this.triggerCouple = triggerCouple;
        } else {
            this.triggerCouple = triggerCouple;
            setAllUnSelect();
            this.triggerCouple.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        triggerEdge.setRxMsgSelect(false);
        triggerCouple.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerEdge{" +
                "triggerSource=" + triggerSource +
                ", triggerEdge=" + triggerEdge +
                ", triggerCouple=" + triggerCouple +
                '}';
    }
}
