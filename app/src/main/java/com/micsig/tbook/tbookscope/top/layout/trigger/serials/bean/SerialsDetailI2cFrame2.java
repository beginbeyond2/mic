package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cFrame2 implements ISerialsDetail {
    private DataBean i2cFrame2Addr;
    private DataBean i2cFrame2Data1;
    private DataBean i2cFrame2Data2;
    private String i2cFrame2AddrTitle;
    private String i2cFrame2Data1Title;
    private String i2cFrame2Data2Title;

    public String getI2cFrame2AddrTitle() {
        return i2cFrame2AddrTitle;
    }

    public void setI2cFrame2AddrTitle(String i2cFrame2AddrTitle) {
        this.i2cFrame2AddrTitle = i2cFrame2AddrTitle;
    }

    public String getI2cFrame2Data1Title() {
        return i2cFrame2Data1Title;
    }

    public void setI2cFrame2Data1Title(String i2cFrame2Data1Title) {
        this.i2cFrame2Data1Title = i2cFrame2Data1Title;
    }

    public String getI2cFrame2Data2Title() {
        return i2cFrame2Data2Title;
    }

    public void setI2cFrame2Data2Title(String i2cFrame2Data2Title) {
        this.i2cFrame2Data2Title = i2cFrame2Data2Title;
    }

    public DataBean getI2cFrame2Addr() {
        return i2cFrame2Addr;
    }

    public void setI2cFrame2Addr(int digits, String value) {
        if (this.i2cFrame2Addr == null) {
            this.i2cFrame2Addr = new DataBean();
        }
        this.i2cFrame2Addr.setDigits(digits);
        this.i2cFrame2Addr.setValue(value);
    }

    public DataBean getI2cFrame2Data1() {
        return i2cFrame2Data1;
    }

    public void setI2cFrame2Data1(int digits, String value) {
        if (this.i2cFrame2Data1 == null) {
            this.i2cFrame2Data1 = new DataBean();
        }
        this.i2cFrame2Data1.setDigits(digits);
        this.i2cFrame2Data1.setValue(value);
    }

    public DataBean getI2cFrame2Data2() {
        return i2cFrame2Data2;
    }

    public void setI2cFrame2Data2(int digits, String value) {
        if (this.i2cFrame2Data2 == null) {
            this.i2cFrame2Data2 = new DataBean();
        }
        this.i2cFrame2Data2.setDigits(digits);
        this.i2cFrame2Data2.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailI2cFrame2{" +
                "i2cFrame2Addr='" + i2cFrame2Addr + '\'' +
                ", i2cFrame2Data1='" + i2cFrame2Data1 + '\'' +
                ", i2cFrame2Data2='" + i2cFrame2Data2 + '\'' +
                '}';
    }
}
