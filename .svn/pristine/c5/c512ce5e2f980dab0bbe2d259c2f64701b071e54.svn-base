package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerCommon implements ITriggerDetail {
    private RxStringWithSelect holdoffTime;
    private TopBeanChannel mode;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerCommon topMsgTriggerCommon = (TopMsgTriggerCommon) super.clone();
        topMsgTriggerCommon.holdoffTime = (RxStringWithSelect) topMsgTriggerCommon.holdoffTime.clone();
        topMsgTriggerCommon.mode = (TopBeanChannel) topMsgTriggerCommon.mode.clone();
        return topMsgTriggerCommon;
    }

    public RxStringWithSelect getHoldoffTime() {
        return holdoffTime;
    }

    @Override
    public TopBeanChannel getTriggerSource() {
        return null;
    }

    public void setHoldoffTime(String holdoffTime) {
        if (this.holdoffTime == null) {
            this.holdoffTime = new RxStringWithSelect(holdoffTime);
        } else {
            this.holdoffTime.setValue(holdoffTime);
            setAllUnSelect();
            this.holdoffTime.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getMode() {
        return mode;
    }

    public void setMode(TopBeanChannel mode) {
        if (this.mode == null) {
            this.mode = mode;
        } else {
            this.mode = mode;
            setAllUnSelect();
            this.mode.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        holdoffTime.setRxMsgSelect(false);
        mode.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerCommon{" +
                "holdoffTime='" + holdoffTime + '\'' +
                ", mode=" + mode +
                '}';
    }
}
