package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailI2cRomData implements ISerialsDetail {
    private TopBeanChannel i2cRomDataCondition;
    private DataBean i2cRomDataData;
    private String i2cRomDataConditionTitle;
    private String i2cRomDataDataTitle;

    public String getI2cRomDataConditionTitle() {
        return i2cRomDataConditionTitle;
    }

    public void setI2cRomDataConditionTitle(String i2cRomDataConditionTitle) {
        this.i2cRomDataConditionTitle = i2cRomDataConditionTitle;
    }

    public String getI2cRomDataDataTitle() {
        return i2cRomDataDataTitle;
    }

    public void setI2cRomDataDataTitle(String i2cRomDataDataTitle) {
        this.i2cRomDataDataTitle = i2cRomDataDataTitle;
    }

    public TopBeanChannel getI2cRomDataCondition() {
        return i2cRomDataCondition;
    }

    public void setI2cRomDataCondition(TopBeanChannel i2cRomDataCondition) {
        this.i2cRomDataCondition = i2cRomDataCondition;
    }

    public DataBean getI2cRomDataData() {
        return i2cRomDataData;
    }

    public void setI2cRomDataData(int digits, String value) {
        if (this.i2cRomDataData == null) {
            this.i2cRomDataData = new DataBean();
        }
        this.i2cRomDataData.setDigits(digits);
        this.i2cRomDataData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailI2cRomData{" +
                "i2cRomDataCondition=" + i2cRomDataCondition +
                ", i2cRomDataData='" + i2cRomDataData + '\'' +
                '}';
    }
}
