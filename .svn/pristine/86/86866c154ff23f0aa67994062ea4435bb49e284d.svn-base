package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgTrigger implements Cloneable {
    private TopAllBeanTitle triggerTitle;
    private ITriggerDetail triggerDetail;
    private boolean isFromEventBus;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTrigger topMsgTrigger = (TopMsgTrigger) super.clone();
        topMsgTrigger.triggerTitle = (TopAllBeanTitle) topMsgTrigger.triggerTitle.clone();
        topMsgTrigger.triggerDetail = (ITriggerDetail) topMsgTrigger.triggerDetail.clone();
        return topMsgTrigger;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getTriggerTitle() {
        return triggerTitle;
    }

    public void setTriggerTitle(TopAllBeanTitle triggerTitle) {

        if (this.triggerDetail == null) {
            this.triggerTitle = triggerTitle;
        } else {
            this.triggerTitle = triggerTitle;
            this.triggerTitle.setRxMsgSelect(true);
        }
    }

    public ITriggerDetail getTriggerDetail() {
        return triggerDetail;
    }

    public void setTriggerDetail(ITriggerDetail triggerDetail) {
        this.triggerDetail = triggerDetail;
    }

    @Override
    public String toString() {
        return "TopMsgTrigger{" +
                "triggerTitle=" + triggerTitle +
                ", triggerDetail=" + triggerDetail +
                '}';
    }
}
