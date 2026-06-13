package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2c10WriteFrame implements ISerialsDetail {
    private DataBean i2c10WriteFrameAddr;
    private DataBean i2c10WriteFrameData;
    private String i2c10WriteFrameAddrTitle;
    private String i2c10WriteFrameDataTitle;

    public String getI2c10WriteFrameAddrTitle() {
        return i2c10WriteFrameAddrTitle;
    }

    public void setI2c10WriteFrameAddrTitle(String i2c10WriteFrameAddrTitle) {
        this.i2c10WriteFrameAddrTitle = i2c10WriteFrameAddrTitle;
    }

    public String getI2c10WriteFrameDataTitle() {
        return i2c10WriteFrameDataTitle;
    }

    public void setI2c10WriteFrameDataTitle(String i2c10WriteFrameDataTitle) {
        this.i2c10WriteFrameDataTitle = i2c10WriteFrameDataTitle;
    }

    public DataBean getI2c10WriteFrameAddr() {
        return i2c10WriteFrameAddr;
    }

    public void setI2c10WriteFrameAddr(int digits, String value) {
        if (this.i2c10WriteFrameAddr == null) {
            this.i2c10WriteFrameAddr = new DataBean();
        }
        this.i2c10WriteFrameAddr.setDigits(digits);
        this.i2c10WriteFrameAddr.setValue(value);
    }

    public DataBean getI2c10WriteFrameData() {
        return i2c10WriteFrameData;
    }

    public void setI2c10WriteFrameData(int digits, String value) {
        if (this.i2c10WriteFrameData == null) {
            this.i2c10WriteFrameData = new DataBean();
        }
        this.i2c10WriteFrameData.setDigits(digits);
        this.i2c10WriteFrameData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailI2c10WriteFrame{" +
                "i2c10WriteFrameAddr='" + i2c10WriteFrameAddr + '\'' +
                ", i2c10WriteFrameData='" + i2c10WriteFrameData + '\'' +
                '}';
    }
}
