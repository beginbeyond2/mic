package com.micsig.tbook.tbookscope.top.layout.sample;

import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;
import com.micsig.tbook.ui.util.StrUtil;

/**
 * Created by yangj on 2017/5/27.
 */

public class TopMsgSampleMode {
    private boolean isFromEventBus;
    private TopBeanChannel sample;
    private String detail;
    private int sampleDetailIndex;
    private boolean[] sampleEnable = new boolean[]{true, true, true, true};

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopBeanChannel getSample() {
        return sample;
    }

    public void setSample(TopBeanChannel sample) {
        this.sample = sample;
        if (StrUtil.isLangEn() && sample.getIndex() == 1) {
            sample.setSimpleText(sample.getText().substring(0, 3) + ".");
        }else if (StrUtil.isLangEn() && sample.getIndex() == 2){
            sample.setSimpleText(sample.getText().substring(0,3)+".");
        } else {
            sample.setSimpleText("");
        }
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public int getSampleDetailIndex() {
        return sampleDetailIndex;
    }

    public void setSampleDetailIndex(int sampleDetailIndex) {
        this.sampleDetailIndex = sampleDetailIndex;
    }

    public boolean[] getSampleEnable() {
        return sampleEnable;
    }

    public boolean setSampleEnable(int index, boolean sampleEnable) {
        boolean change = false;
        if (this.sampleEnable[index] != sampleEnable) {
            change = true;
        }
        this.sampleEnable[index] = sampleEnable;
        return change;
    }

    public boolean setSampleEnable(boolean sample1, boolean sample2) {
        boolean change = false;
        if (this.sampleEnable[1] != sample1) {
            change = true;
        }
        if (this.sampleEnable[2] != sample2) {
            change = true;
        }
        this.sampleEnable[1] = sample1;
        this.sampleEnable[2] = sample2;
        return change;
    }

    public boolean setSampleEnable(boolean sample1, boolean sample2, boolean sample3) {
        boolean change = false;
        if (this.sampleEnable[1] != sample1) {
            change = true;
        }
        if (this.sampleEnable[2] != sample2) {
            change = true;
        }
        if (this.sampleEnable[3] != sample3) {
            change = true;
        }
        this.sampleEnable[1] = sample1;
        this.sampleEnable[2] = sample2;
        this.sampleEnable[3] = sample3;
        return change;
    }

    public boolean setSampleEnable(boolean sample0, boolean sample1, boolean sample2, boolean sample3) {
        boolean change = false;
        if (this.sampleEnable[0] != sample0) {
            change = true;
        }
        if (this.sampleEnable[1] != sample1) {
            change = true;
        }
        if (this.sampleEnable[2] != sample2) {
            change = true;
        }
        if (this.sampleEnable[3] != sample3) {
            change = true;
        }
        this.sampleEnable[0] = sample0;
        this.sampleEnable[1] = sample1;
        this.sampleEnable[2] = sample2;
        this.sampleEnable[3] = sample3;
        return change;
    }

    @Override
    public String toString() {
        return "TopMsgSample{" +
                "sample=" + sample +
                ", detail='" + detail + '\'' +
                ", sampleDetailIndex='" + sampleDetailIndex + '\'' +
                '}';
    }
}
