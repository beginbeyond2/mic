package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailUartxData implements ISerialsDetail {
    private TopBeanChannel uartxDataCondition;
    private DataBean uartxDataEdit;
    private String uartxDataConditionTitle;
    private String uartxDataEditTitle;

    public String getUartxDataConditionTitle() {
        return uartxDataConditionTitle;
    }

    public void setUartxDataConditionTitle(String uartxDataConditionTitle) {
        this.uartxDataConditionTitle = uartxDataConditionTitle;
    }

    public String getUartxDataEditTitle() {
        return uartxDataEditTitle;
    }

    public void setUartxDataEditTitle(String uartxDataEditTitle) {
        this.uartxDataEditTitle = uartxDataEditTitle;
    }

    public TopBeanChannel getUartxDataCondition() {
        return uartxDataCondition;
    }

    public void setUartxDataCondition(TopBeanChannel uartxDataCondition) {
            this.uartxDataCondition = uartxDataCondition;
    }

    public DataBean getUartxDataEdit() {
        return uartxDataEdit;
    }

    public void setUartxDataEdit(int digits, String value) {
        if (this.uartxDataEdit == null) {
            this.uartxDataEdit = new DataBean();
        }
        this.uartxDataEdit.setDigits(digits);
        this.uartxDataEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailUartxData{" +
                "uartxDataCondition=" + uartxDataCondition +
                ", uartxDataEdit='" + uartxDataEdit + '\'' +
                '}';
    }
}
