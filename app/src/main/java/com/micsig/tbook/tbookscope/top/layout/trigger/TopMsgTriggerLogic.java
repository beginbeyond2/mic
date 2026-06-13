package com.micsig.tbook.tbookscope.top.layout.trigger;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgTriggerLogic implements ITriggerDetail {
    private TopBeanChannel ch1;
    private TopBeanChannel ch2;
    private TopBeanChannel ch3;
    private TopBeanChannel ch4;
    private TopBeanChannel ch5;
    private TopBeanChannel ch6;
    private TopBeanChannel ch7;
    private TopBeanChannel ch8;
    private TopBeanChannel triggerLogic;
    private TopBeanChannel condition;
    private RxStringWithSelect timeHighDetail;
    private RxStringWithSelect timeLowDetail;
    private RxStringWithSelect logic;

    @Override
    public Object clone() throws CloneNotSupportedException {
        TopMsgTriggerLogic topMsgTriggerLogic = (TopMsgTriggerLogic) super.clone();
        topMsgTriggerLogic.ch1 = (TopBeanChannel) topMsgTriggerLogic.ch1.clone();
        topMsgTriggerLogic.ch2 = (TopBeanChannel) topMsgTriggerLogic.ch2.clone();
        topMsgTriggerLogic.ch3 = (TopBeanChannel) topMsgTriggerLogic.ch3.clone();
        topMsgTriggerLogic.ch4 = (TopBeanChannel) topMsgTriggerLogic.ch4.clone();
        topMsgTriggerLogic.ch5 = (TopBeanChannel) topMsgTriggerLogic.ch5.clone();
        topMsgTriggerLogic.ch6 = (TopBeanChannel) topMsgTriggerLogic.ch6.clone();
        topMsgTriggerLogic.ch7 = (TopBeanChannel) topMsgTriggerLogic.ch7.clone();
        topMsgTriggerLogic.ch8 = (TopBeanChannel) topMsgTriggerLogic.ch8.clone();
        topMsgTriggerLogic.triggerLogic = (TopBeanChannel) topMsgTriggerLogic.triggerLogic.clone();
        topMsgTriggerLogic.condition = (TopBeanChannel) topMsgTriggerLogic.condition.clone();
        topMsgTriggerLogic.timeHighDetail = (RxStringWithSelect) topMsgTriggerLogic.timeHighDetail.clone();
        topMsgTriggerLogic.timeLowDetail = (RxStringWithSelect) topMsgTriggerLogic.timeLowDetail.clone();
        topMsgTriggerLogic.logic = (RxStringWithSelect) topMsgTriggerLogic.logic.clone();
        return topMsgTriggerLogic;
    }

    @Override
    public TopBeanChannel getTriggerSource() {
        return null;
    }

    public TopBeanChannel getCh1() {
        return ch1;
    }

    public void setCh1(TopBeanChannel ch1) {
        if (this.ch1 == null) {
            this.ch1 = ch1;
        } else {
            this.ch1 = ch1;
            setAllUnSelect();
            this.ch1.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh2() {
        return ch2;
    }

    public void setCh2(TopBeanChannel ch2) {
        if (this.ch2 == null) {
            this.ch2 = ch2;
        } else {
            this.ch2 = ch2;
            setAllUnSelect();
            this.ch2.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh3() {
        return ch3;
    }

    public void setCh3(TopBeanChannel ch3) {
        if (this.ch3 == null) {
            this.ch3 = ch3;
        } else {
            this.ch3 = ch3;
            setAllUnSelect();
            this.ch3.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh4() {
        return ch4;
    }

    public void setCh4(TopBeanChannel ch4) {
        if (this.ch4 == null) {
            this.ch4 = ch4;
        } else {
            this.ch4 = ch4;
            setAllUnSelect();
            this.ch4.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh5() {
        return ch5;
    }

    public void setCh5(TopBeanChannel ch5) {
        if (this.ch5 == null) {
            this.ch5 = ch5;
        } else {
            this.ch5 = ch5;
            setAllUnSelect();
            this.ch5.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh6() {
        return ch6;
    }

    public void setCh6(TopBeanChannel ch6) {
        if (this.ch6 == null) {
            this.ch6 = ch6;
        } else {
            this.ch6 = ch6;
            setAllUnSelect();
            this.ch6.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getCh7() {
        return ch7;
    }

    public void setCh7(TopBeanChannel ch7) {
        if (this.ch7 == null) {
            this.ch7 = ch7;
        } else {
            this.ch7 = ch7;
            setAllUnSelect();
            this.ch7.setRxMsgSelect(true);
        }
    }


    public TopBeanChannel getCh8() {
        return ch8;
    }

    public void setCh8(TopBeanChannel ch8) {
        if (this.ch8 == null) {
            this.ch8 = ch8;
        } else {
            this.ch8 = ch8;
            setAllUnSelect();
            this.ch8.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getTriggerLogic() {
        return triggerLogic;
    }

    public void setTriggerLogic(TopBeanChannel triggerLogic) {
        if (this.triggerLogic == null) {
            this.triggerLogic = triggerLogic;
        } else {
            this.triggerLogic = triggerLogic;
            setAllUnSelect();
            this.triggerLogic.setRxMsgSelect(true);
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

    public RxStringWithSelect getLogic() {
        return logic;
    }

    public void setLogic(String logic) {
        if (this.logic == null) {
            this.logic = new RxStringWithSelect(logic);
        } else {
            this.logic.setValue(logic);
            setAllUnSelect();
            this.logic.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        ch1.setRxMsgSelect(false);
        ch2.setRxMsgSelect(false);
        ch3.setRxMsgSelect(false);
        ch4.setRxMsgSelect(false);
        ch5.setRxMsgSelect(false);
        ch6.setRxMsgSelect(false);
        ch7.setRxMsgSelect(false);
        ch8.setRxMsgSelect(false);
        triggerLogic.setRxMsgSelect(false);
        condition.setRxMsgSelect(false);
        timeHighDetail.setRxMsgSelect(false);
        timeLowDetail.setRxMsgSelect(false);
        logic.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgTriggerLogic{" +
                "ch1=" + ch1 +
                ", ch2=" + ch2 +
                ", ch3=" + ch3 +
                ", ch4=" + ch4 +
                ", ch5=" + ch5 +
                ", ch6=" + ch6 +
                ", ch7=" + ch7 +
                ", ch8=" + ch8 +
                ", triggerLogic=" + triggerLogic +
                ", condition=" + condition +
                ", logic='" + logic +
                ", timeHighDetail='" + timeHighDetail +
                ", timeLowDetail='" + timeLowDetail + '\'' +
                '}';
    }
}
