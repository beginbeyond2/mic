package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailSpiData implements ISerialsDetail {
    private DataBean spiDataData;
    private String spiDataDataTitle;

    public String getSpiDataDataTitle() {
        return spiDataDataTitle;
    }

    public void setSpiDataDataTitle(String spiDataDataTitle) {
        this.spiDataDataTitle = spiDataDataTitle;
    }

    public DataBean getSpiDataData() {
        return spiDataData;
    }

    public void setSpiDataData(int digits, String value) {
        if (this.spiDataData == null) {
            this.spiDataData = new DataBean();
        }
        this.spiDataData.setDigits(digits);
        this.spiDataData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailSpiData{" +
                "spiDataData='" + spiDataData + '\'' +
                '}';
    }
}
