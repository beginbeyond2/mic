package com.micsig.tbook.tbookscope.top.layout.sample;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

public class TopMsgSample {
    private TopAllBeanTitle sampleTitle;
    private ISampleDetail sampleDetail;
    private boolean isFromEventBus;

    public TopAllBeanTitle getSampleTitle() {
        return sampleTitle;
    }

    public void setSampleTitle(TopAllBeanTitle sampleTitle) {
        if (sampleTitle == null) {
            this.sampleTitle = sampleTitle;
        } else {
            this.sampleTitle = sampleTitle;
            setAllUnSelect();
            this.sampleTitle.setRxMsgSelect(true);
        }
    }

    public ISampleDetail getSampleDetail() {
        return sampleDetail;
    }

    public void setSampleDetail(ISampleDetail sampleDetail) {
        this.sampleDetail = sampleDetail;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    private void setAllUnSelect() {
        sampleTitle.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgSample{" +
                "isFromEventBus=" + isFromEventBus +
                ", sampleTitle=" + sampleTitle +
                ", sampleDetail=" + sampleDetail +
                '}';
    }
}
