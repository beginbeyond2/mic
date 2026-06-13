package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.tbookscope.R;
import com.micsig.tbook.tbookscope.util.App;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerVideo implements ITriggerDetail {
    private TopBeanChannel triggerSource;
    private TopBeanChannel polar;
    private TopBeanChannel standard;
    private TopBeanChannel trigger;
    private TopBeanChannel frequency;
    private RxStringWithSelect lineDetail;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerVideo topMsgTriggerVideo = (TopMsgTriggerVideo) super.clone();
        topMsgTriggerVideo.triggerSource = (TopBeanChannel) topMsgTriggerVideo.triggerSource.clone();
        topMsgTriggerVideo.polar = (TopBeanChannel) topMsgTriggerVideo.polar.clone();
        topMsgTriggerVideo.standard = (TopBeanChannel) topMsgTriggerVideo.standard.clone();
        topMsgTriggerVideo.trigger = (TopBeanChannel) topMsgTriggerVideo.trigger.clone();
        topMsgTriggerVideo.frequency = (TopBeanChannel) topMsgTriggerVideo.frequency.clone();
        topMsgTriggerVideo.lineDetail = (RxStringWithSelect) topMsgTriggerVideo.lineDetail.clone();
        return topMsgTriggerVideo;
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

    public TopBeanChannel getStandard() {
        return standard;
    }

    public void setStandard(TopBeanChannel standard) {
        if (this.standard == null) {
            this.standard = standard;
        } else {
            this.standard = standard;
            setAllUnSelect();
            this.standard.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getTrigger() {
        return trigger;
    }

    public void setTrigger(TopBeanChannel trigger) {
        if (this.trigger == null) {
            this.trigger = trigger;
        } else {
            this.trigger = trigger;
            setAllUnSelect();
            this.trigger.setRxMsgSelect(true);
        }
    }

    public boolean isTriggerLine() {
        String[] ss = App.get().getResources().getStringArray(R.array.triggerVideoTriggerMore);
        String s = ss[ss.length - 1];
        return s.equals(trigger.getText());
    }

    public TopBeanChannel getFrequency() {
        return frequency;
    }

    public void setFrequency(TopBeanChannel frequency) {
        if (this.frequency == null) {
            this.frequency = frequency;
        } else {
            this.frequency = frequency;
            setAllUnSelect();
            this.frequency.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getLineDetail() {
        return lineDetail;
    }

    public void setLineDetail(String lineDetail) {
        if (this.lineDetail == null) {
            this.lineDetail = new RxStringWithSelect(lineDetail);
        } else {
            this.lineDetail.setValue(lineDetail);
            setAllUnSelect();
            this.lineDetail.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        triggerSource.setRxMsgSelect(false);
        polar.setRxMsgSelect(false);
        standard.setRxMsgSelect(false);
        trigger.setRxMsgSelect(false);
        frequency.setRxMsgSelect(false);
        lineDetail.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerVideo{" +
                "triggerSource=" + triggerSource +
                ", polar=" + polar +
                ", standard=" + standard +
                ", trigger=" + trigger +
                ", frequency=" + frequency +
                ", lineDetail='" + lineDetail + '\'' +
                '}';
    }
}
