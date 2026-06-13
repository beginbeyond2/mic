package com.micsig.tbook.tbookscope.top.layout.measure;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgMeasure {
    private TopAllBeanTitle measureTitle;
    private IMeasureDetail measureDetail;
    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getMeasureTitle() {
        return measureTitle;
    }

    public void setMeasureTitle(TopAllBeanTitle measureTitle) {
        if (this.measureTitle == null) {
            this.measureTitle = measureTitle;
        } else {
            this.measureTitle = measureTitle;
            this.measureTitle.setRxMsgSelect(true);
        }
    }

    public IMeasureDetail getMeasureDetail() {
        return measureDetail;
    }

    public void setMeasureDetail(IMeasureDetail measureDetail) {
        this.measureDetail = measureDetail;
    }

    @Override
    public String toString() {
        return "TopMsgMeasure{" +
                "measureTitle=" + measureTitle +
                ", measureDetail=" + measureDetail +
                '}';
    }
}
