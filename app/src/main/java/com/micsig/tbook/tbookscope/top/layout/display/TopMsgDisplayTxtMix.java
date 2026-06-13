package com.micsig.tbook.tbookscope.top.layout.display;


/**
 * Created by limh on 2024/8/8.
 */

public class TopMsgDisplayTxtMix implements IDisplayDetail {
    private boolean s1Select, s2Select, s3Select, s4Select;

    public boolean isS1Select() {
        return s1Select;
    }

    public void setS1Select(boolean s1Select) {
        this.s1Select = s1Select;
    }

    public boolean isS2Select() {
        return s2Select;
    }

    public void setS2Select(boolean s2Select) {
        this.s2Select = s2Select;
    }

    public boolean isS3Select() {
        return s3Select;
    }

    public void setS3Select(boolean s3Select) {
        this.s3Select = s3Select;
    }

    public boolean isS4Select() {
        return s4Select;
    }

    public void setS4Select(boolean s4Select) {
        this.s4Select = s4Select;
    }

    @Override
    public String toString() {
        return "TopMsgDisplayTxtMix{" +
                "s1Select=" + s1Select +
                ", s2Select=" + s2Select +
                ", s3Select=" + s3Select +
                ", s4Select=" + s4Select +
                '}';
    }
}
