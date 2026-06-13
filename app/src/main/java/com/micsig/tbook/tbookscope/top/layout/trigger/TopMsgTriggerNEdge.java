package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerNEdge implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel nEdgeSlope;
    private RxStringWithSelect nEdgeTime;
    private RxStringWithSelect nEdgeDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerNEdge topMsgTriggerNEdge = (TopMsgTriggerNEdge) super.clone();
        topMsgTriggerNEdge.triggerSource = (TopBeanChannel) topMsgTriggerNEdge.triggerSource.clone();
        topMsgTriggerNEdge.nEdgeSlope = (TopBeanChannel) topMsgTriggerNEdge.nEdgeSlope.clone();
        topMsgTriggerNEdge.nEdgeTime = (RxStringWithSelect) topMsgTriggerNEdge.nEdgeTime.clone();
        topMsgTriggerNEdge.nEdgeDetail = (RxStringWithSelect) topMsgTriggerNEdge.nEdgeDetail.clone();
        return topMsgTriggerNEdge;
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

    public TopBeanChannel getnEdgeSlope() {
        return nEdgeSlope;
    }

    public void setnEdgeSlope(TopBeanChannel nEdgeSlope) {
        if (this.nEdgeSlope == null) {
            this.nEdgeSlope = nEdgeSlope;
        } else {
            this.nEdgeSlope = nEdgeSlope;
            setAllUnSelect();
            this.nEdgeSlope.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getnEdgeTime() {
        return nEdgeTime;
    }

    public void setnEdgeTime(String nEdgeTime) {
        if (this.nEdgeTime == null) {
            this.nEdgeTime = new RxStringWithSelect(nEdgeTime);
        } else {
            this.nEdgeTime.setValue(nEdgeTime);
            setAllUnSelect();
            this.nEdgeTime.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getnEdgeDetail() {
        return nEdgeDetail;
    }

    public void setnEdgeDetail(String nEdgeDetail) {
        if (this.nEdgeDetail == null) {
            this.nEdgeDetail = new RxStringWithSelect(nEdgeDetail);
        } else {
            this.nEdgeDetail.setValue(nEdgeDetail);
            setAllUnSelect();
            this.nEdgeDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        nEdgeSlope.setRxMsgSelect(false);
        nEdgeTime.setRxMsgSelect(false);
        nEdgeDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerNEdge{" +
                "triggerSource=" + triggerSource +
                ", nEdgeSlope=" + nEdgeSlope +
                ", nEdgeTime='" + nEdgeTime + '\'' +
                ", nEdgeDetail='" + nEdgeDetail + '\'' +
                '}';
    }
}
