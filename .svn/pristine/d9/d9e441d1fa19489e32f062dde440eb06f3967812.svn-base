package com.micsig.tbook.tbookscope.top.layout.sample;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

public class TopMsgSampleSegmented {
    private boolean isFromEventBus;
    private RxBooleanWithSelect state;
    private TopBeanChannel number;
    private RxStringWithSelect userDefine;
    private TopBeanChannel display;
    private RxStringWithSelect start;
    private RxStringWithSelect end;
    private TopBeanChannel order;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public RxBooleanWithSelect getState() {
        return state;
    }

    public void setState(boolean state) {
        if (this.state == null) {
            this.state = new RxBooleanWithSelect(state);
        } else {
            this.state.setValue(state);
            setAllUnSelect();
            this.state.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getNumber() {
        return number;
    }

    public void setNumber(TopBeanChannel number) {
        if (this.number == null) {
            this.number = number;
        } else {
            this.number = number;
            setAllUnSelect();
            this.number.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getUserDefine() {
        return userDefine;
    }

    public void setUserDefine(String userDefine) {
        if (this.userDefine == null) {
            this.userDefine = new RxStringWithSelect(userDefine);
        } else {
            this.userDefine.setValue(userDefine);
            setAllUnSelect();
            this.userDefine.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getDisplay() {
        return display;
    }

    public void setDisplay(TopBeanChannel display) {
        if (this.display == null) {
            this.display = display;
        } else {
            this.display = display;
            setAllUnSelect();
            this.display.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getStart() {
        return start;
    }

    public void setStart(String start) {
        if (this.start == null) {
            this.start = new RxStringWithSelect(start);
        } else {
            this.start.setValue(start);
            setAllUnSelect();
            this.start.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getEnd() {
        return end;
    }

    public void setEnd(String end) {
        if (this.end == null) {
            this.end = new RxStringWithSelect(end);
        } else {
            this.end.setValue(end);
            setAllUnSelect();
            this.end.setRxMsgSelect(true);
        }
    }

    public TopBeanChannel getOrder() {
        return order;
    }

    public void setOrder(TopBeanChannel order) {
        if (this.order == null) {
            this.order = order;
        } else {
            this.order = order;
            setAllUnSelect();
            this.order.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        state.setRxMsgSelect(false);
        number.setRxMsgSelect(false);
        userDefine.setRxMsgSelect(false);
        display.setRxMsgSelect(false);
        start.setRxMsgSelect(false);
        end.setRxMsgSelect(false);
        order.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgSampleSegmented{" +
                "state=" + state +
                ", number=" + number +
                ", userDefine=" + userDefine +
                ", display=" + display +
                ", start=" + start +
                ", end=" + end +
                ", order=" + order +
                '}';
    }
}
