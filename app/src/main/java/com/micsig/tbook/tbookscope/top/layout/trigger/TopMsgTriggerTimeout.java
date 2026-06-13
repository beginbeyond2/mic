package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerTimeout implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel polar;
    private RxStringWithSelect overTimeTimeDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerTimeout topMsgTriggerTimeout = (TopMsgTriggerTimeout) super.clone();
        topMsgTriggerTimeout.triggerSource = (TopBeanChannel) topMsgTriggerTimeout.triggerSource.clone();
        topMsgTriggerTimeout.polar = (TopBeanChannel) topMsgTriggerTimeout.polar.clone();
        topMsgTriggerTimeout.overTimeTimeDetail = (RxStringWithSelect) topMsgTriggerTimeout.overTimeTimeDetail.clone();
        return topMsgTriggerTimeout;
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

    public RxStringWithSelect getOverTimeTimeDetail() {
        return overTimeTimeDetail;
    }

    public void setOverTimeTimeDetail(String overTimeTimeDetail) {
        if (this.overTimeTimeDetail == null) {
            this.overTimeTimeDetail = new RxStringWithSelect(overTimeTimeDetail);
        } else {
            this.overTimeTimeDetail.setValue(overTimeTimeDetail);
            setAllUnSelect();
            this.overTimeTimeDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        polar.setRxMsgSelect(false);
        overTimeTimeDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerTimeout{" +
                "triggerSource=" + triggerSource +
                ", polar=" + polar +
                ", overTimeTimeDetail='" + overTimeTimeDetail + '\'' +
                '}';
    }
}
