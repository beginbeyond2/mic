package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429LabelSdi implements ISerialsDetail {
    private DataBean arinc429LabelSdiLabel;
    private DataBean arinc429LabelSdiSdi;
    private String arinc429LabelSdiLabelTitle;
    private String arinc429LabelSdiSdiTitle;

    public String getArinc429LabelSdiLabelTitle() {
        return arinc429LabelSdiLabelTitle;
    }

    public void setArinc429LabelSdiLabelTitle(String arinc429LabelSdiLabelTitle) {
        this.arinc429LabelSdiLabelTitle = arinc429LabelSdiLabelTitle;
    }

    public String getArinc429LabelSdiSdiTitle() {
        return arinc429LabelSdiSdiTitle;
    }

    public void setArinc429LabelSdiSdiTitle(String arinc429LabelSdiSdiTitle) {
        this.arinc429LabelSdiSdiTitle = arinc429LabelSdiSdiTitle;
    }

    public DataBean getArinc429LabelSdiLabel() {
        return arinc429LabelSdiLabel;
    }

    public void setArinc429LabelSdiLabel(int digits, String value) {
        if (this.arinc429LabelSdiLabel == null) {
            this.arinc429LabelSdiLabel = new DataBean();
        }
        this.arinc429LabelSdiLabel.setDigits(digits);
        this.arinc429LabelSdiLabel.setValue(value);
    }

    public DataBean getArinc429LabelSdiSdi() {
        return arinc429LabelSdiSdi;
    }

    public void setArinc429LabelSdiSdi(int digits, String value) {
        if (this.arinc429LabelSdiSdi == null) {
            this.arinc429LabelSdiSdi = new DataBean();
        }
        this.arinc429LabelSdiSdi.setDigits(digits);
        this.arinc429LabelSdiSdi.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailArinc429LabelSdi{" +
                "arinc429LabelSdiLabel='" + arinc429LabelSdiLabel + '\'' +
                ", arinc429LabelSdiSdi='" + arinc429LabelSdiSdi + '\'' +
                '}';
    }
}
