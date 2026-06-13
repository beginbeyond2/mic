package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Sdi implements ISerialsDetail {
    private DataBean arinc429SdiLabel;
    private String arinc429SdiLabelTitle;

    public String getArinc429SdiLabelTitle() {
        return arinc429SdiLabelTitle;
    }

    public void setArinc429SdiLabelTitle(String arinc429SdiLabelTitle) {
        this.arinc429SdiLabelTitle = arinc429SdiLabelTitle;
    }

    public DataBean getArinc429SdiLabel() {
        return arinc429SdiLabel;
    }

    public void setArinc429SdiLabel(int digits, String value) {
        if (this.arinc429SdiLabel == null) {
            this.arinc429SdiLabel = new DataBean();
        }
        this.arinc429SdiLabel.setDigits(digits);
        this.arinc429SdiLabel.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailArinc429Sdi{" +
                "arinc429SdiLabel='" + arinc429SdiLabel + '\'' +
                '}';
    }
}
