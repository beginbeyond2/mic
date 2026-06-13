package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUart0Data implements ISerialsDetail {
    private TopBeanChannel uart0DataCondition;
    private DataBean uart0DataEdit;
    private String uart0DataConditionTitle;
    private String uart0DataEditTitle;

    public String getUart0DataConditionTitle() {
        return uart0DataConditionTitle;
    }

    public void setUart0DataConditionTitle(String uart0DataConditionTitle) {
        this.uart0DataConditionTitle = uart0DataConditionTitle;
    }

    public String getUart0DataEditTitle() {
        return uart0DataEditTitle;
    }

    public void setUart0DataEditTitle(String uart0DataEditTitle) {
        this.uart0DataEditTitle = uart0DataEditTitle;
    }

    public TopBeanChannel getUart0DataCondition() {
        return uart0DataCondition;
    }

    public void setUart0DataCondition(TopBeanChannel uart0DataCondition) {
        this.uart0DataCondition = uart0DataCondition;
    }

    public DataBean getUart0DataEdit() {
        return uart0DataEdit;
    }

    public void setUart0DataEdit(int digits, String value) {
        if (this.uart0DataEdit == null) {
            this.uart0DataEdit = new DataBean();
        }
        this.uart0DataEdit.setDigits(digits);
        this.uart0DataEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailUart0Data{" +
                "uart0DataCondition=" + uart0DataCondition +
                ", uart0DataEdit='" + uart0DataEdit + '\'' +
                '}';
    }
}
