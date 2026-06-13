package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cNoAckInAdr implements ISerialsDetail {
    private DataBean i2cNoAckInAdrData;
    private String i2cNoAckInAdrDataTitle;

    public String getI2cNoAckInAdrDataTitle() {
        return i2cNoAckInAdrDataTitle;
    }

    public void setI2cNoAckInAdrDataTitle(String i2cNoAckInAdrDataTitle) {
        this.i2cNoAckInAdrDataTitle = i2cNoAckInAdrDataTitle;
    }

    public DataBean getI2cNoAckInAdrData() {
        return i2cNoAckInAdrData;
    }

    public void setI2cNoAckInAdrData(int digits, String value) {
        if (this.i2cNoAckInAdrData == null) {
            this.i2cNoAckInAdrData = new DataBean();
        }
        this.i2cNoAckInAdrData.setDigits(digits);
        this.i2cNoAckInAdrData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailI2cNoAckInAdr{" +
                "i2cNoAckInAdrData='" + i2cNoAckInAdrData + '\'' +
                '}';
    }
}
