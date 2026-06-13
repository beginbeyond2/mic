package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailArinc429Data implements ISerialsDetail {
    private DataBean arinc429DataData;
    private String arinc429DataDataTitle;

    public String getArinc429DataDataTitle() {
        return arinc429DataDataTitle;
    }

    public void setArinc429DataDataTitle(String arinc429DataDataTitle) {
        this.arinc429DataDataTitle = arinc429DataDataTitle;
    }

    public DataBean getArinc429DataData() {
        return arinc429DataData;
    }

    public void setArinc429DataData(int digits, String value) {
        if (this.arinc429DataData == null) {
            this.arinc429DataData = new DataBean();
        }
        this.arinc429DataData.setDigits(digits);
        this.arinc429DataData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailArinc429Data{" +
                "arinc429DataData='" + arinc429DataData + '\'' +
                '}';
    }
}
