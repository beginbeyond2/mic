package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Ssm implements ISerialsDetail {
    private DataBean arinc429SsmLabel;
    private String arinc429SsmLabelTitle;

    public String getArinc429SsmLabelTitle() {
        return arinc429SsmLabelTitle;
    }

    public void setArinc429SsmLabelTitle(String arinc429SsmLabelTitle) {
        this.arinc429SsmLabelTitle = arinc429SsmLabelTitle;
    }

    public DataBean getArinc429SsmLabel() {
        return arinc429SsmLabel;
    }

    public void setArinc429SsmLabel(int digits, String value) {
        if (this.arinc429SsmLabel == null) {
            this.arinc429SsmLabel = new DataBean();
        }
        this.arinc429SsmLabel.setDigits(digits);
        this.arinc429SsmLabel.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailArinc429Ssm{" +
                "arinc429SsmLabel='" + arinc429SsmLabel + '\'' +
                '}';
    }
}
