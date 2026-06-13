package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUartData implements ISerialsDetail {
    private TopBeanChannel uartDataCondition;
    private DataBean uartDataEdit;
    private String uartDataConditionTitle;
    private String uartDataEditTitle;

    public String getUartDataConditionTitle() {
        return uartDataConditionTitle;
    }

    public void setUartDataConditionTitle(String uartDataConditionTitle) {
        this.uartDataConditionTitle = uartDataConditionTitle;
    }

    public String getUartDataEditTitle() {
        return uartDataEditTitle;
    }

    public void setUartDataEditTitle(String uartDataEditTitle) {
        this.uartDataEditTitle = uartDataEditTitle;
    }

    public TopBeanChannel getUartDataCondition() {
        return uartDataCondition;
    }

    public void setUartDataCondition(TopBeanChannel uartDataCondition) {
        this.uartDataCondition = uartDataCondition;
    }

    public DataBean getUartDataEdit() {
        return uartDataEdit;
    }

    public void setUartDataEdit(int digits, String value) {
        if (this.uartDataEdit == null) {
            this.uartDataEdit = new DataBean();
        }
        this.uartDataEdit.setDigits(digits);
        this.uartDataEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailUartData{" +
                "uartDataCondition=" + uartDataCondition +
                ", uartDataEdit='" + uartDataEdit + '\'' +
                '}';
    }
}
