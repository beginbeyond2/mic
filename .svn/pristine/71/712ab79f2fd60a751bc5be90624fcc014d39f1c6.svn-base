package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

import com.micsig.tbook.tbookscope.top.popwindow.keyboardnumber.IDigits;

public class DataBean implements IDigits {

    /**
     * DIGITS_10 = 10;//输入是10进制<p/>
     * DIGITS_16 = 16;//输入是16进制<p/>
     * DIGITS_16X = 17;//输入是16进制和x<p/>
     * DIGITS_8 = 8;//输入是8进制<p/>
     * DIGITS_4 = 4;//输入是4进制<p/>
     * DIGITS_2 = 2;//输入是2进制<p/>
     * DIGITS_2X = 3;//输入是2进制和x<p/>
     * DIGITS_0_8 = 9;//输入是0到9
     */
    private int digits = DIGITS_10;
    private String value;

    public DataBean() {
    }

    public DataBean(int digits, String value) {
        this.digits = digits;
        this.value = value;
    }

    public int getDigits() {
        return digits;
    }

    public void setDigits(int digits) {
        this.digits = digits;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "digits=" + digits +
                ", value='" + value + '\'' +
                '}';
    }
}
