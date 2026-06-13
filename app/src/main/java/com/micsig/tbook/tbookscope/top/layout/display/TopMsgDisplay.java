package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplay {
    private TopAllBeanTitle displayTitle;
    private IDisplayDetail displayDetail;
    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(TopAllBeanTitle displayTitle) {
        if (this.displayTitle == null) {
            this.displayTitle = displayTitle;
        } else {
            this.displayTitle = displayTitle;
            this.displayTitle.setRxMsgSelect(true);
        }
    }

    public IDisplayDetail getDisplayDetail() {
        return displayDetail;
    }

    public void setDisplayDetail(IDisplayDetail displayDetail) {
        this.displayDetail = displayDetail;
    }

    @Override
    public String toString() {
        return "TopMsgDisplay{" +
                "displayTitle=" + displayTitle +
                ", displayDetail=" + displayDetail +
                '}';
    }
}
