package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailLinIdData implements ISerialsDetail {
    private DataBean linIdDataId;
    private DataBean linIdDataData;
    private String linIdDataIdTitle;
    private String linIdDataDataTitle;

    public String getLinIdDataIdTitle() {
        return linIdDataIdTitle;
    }

    public void setLinIdDataIdTitle(String linIdDataIdTitle) {
        this.linIdDataIdTitle = linIdDataIdTitle;
    }

    public String getLinIdDataDataTitle() {
        return linIdDataDataTitle;
    }

    public void setLinIdDataDataTitle(String linIdDataDataTitle) {
        this.linIdDataDataTitle = linIdDataDataTitle;
    }

    public DataBean getLinIdDataId() {
        return linIdDataId;
    }

    public void setLinIdDataId(int digits, String value) {
        if (this.linIdDataId == null) {
            this.linIdDataId = new DataBean();
        }
        this.linIdDataId.setDigits(digits);
        this.linIdDataId.setValue(value);
    }

    public DataBean getLinIdDataData() {
        return linIdDataData;
    }

    public void setLinIdDataData(int digits, String value) {
        if (this.linIdDataData == null) {
            this.linIdDataData = new DataBean();
        }
        this.linIdDataData.setDigits(digits);
        this.linIdDataData.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailLinIdData{" +
                "linIdDataId='" + linIdDataId + '\'' +
                ", linIdDataData='" + linIdDataData + '\'' +
                '}';
    }
}
