package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429LabelSsm implements ISerialsDetail {
    private DataBean arinc429LabelSsmLabel;
    private DataBean arinc429LabelSsmSsm;
    private String arinc429LabelSsmLabelTitle;
    private String arinc429LabelSsmSsmTitle;

    public String getArinc429LabelSsmLabelTitle() {
        return arinc429LabelSsmLabelTitle;
    }

    public void setArinc429LabelSsmLabelTitle(String arinc429LabelSsmLabelTitle) {
        this.arinc429LabelSsmLabelTitle = arinc429LabelSsmLabelTitle;
    }

    public String getArinc429LabelSsmSsmTitle() {
        return arinc429LabelSsmSsmTitle;
    }

    public void setArinc429LabelSsmSsmTitle(String arinc429LabelSsmSsmTitle) {
        this.arinc429LabelSsmSsmTitle = arinc429LabelSsmSsmTitle;
    }

    public DataBean getArinc429LabelSsmLabel() {
        return arinc429LabelSsmLabel;
    }

    public void setArinc429LabelSsmLabel(int digits, String value) {
        if (this.arinc429LabelSsmLabel == null) {
            this.arinc429LabelSsmLabel = new DataBean();
        }
        this.arinc429LabelSsmLabel.setDigits(digits);
        this.arinc429LabelSsmLabel.setValue(value);
    }

    public DataBean getArinc429LabelSsmSsm() {
        return arinc429LabelSsmSsm;
    }

    public void setArinc429LabelSsmSsm(int digits, String value) {
        if (this.arinc429LabelSsmSsm == null) {
            this.arinc429LabelSsmSsm = new DataBean();
        }
        this.arinc429LabelSsmSsm.setDigits(digits);
        this.arinc429LabelSsmSsm.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailArinc429LabelSsm{" +
                "arinc429LabelSsmLabel='" + arinc429LabelSsmLabel + '\'' +
                ", arinc429LabelSsmSsm='" + arinc429LabelSsmSsm + '\'' +
                '}';
    }
}
