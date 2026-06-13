package com.micsig.tbook.tbookscope.top.layout.auto;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgAuto {
    private TopAllBeanTitle autoTitle;
    private IAutoDetail autoDetail;
    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getAutoTitle() {
        return autoTitle;
    }

    public void setAutoTitle(TopAllBeanTitle autoTitle) {
        if (autoTitle == null) {
            this.autoTitle = autoTitle;
        } else {
            this.autoTitle = autoTitle;
            this.autoTitle.setRxMsgSelect(true);
        }
    }

    public IAutoDetail getAutoDetail() {
        return autoDetail;
    }

    public void setAutoDetail(IAutoDetail autoDetail) {
        this.autoDetail = autoDetail;
    }

    @Override
    public String toString() {
        return "TopMsgAuto{" +
                "autoTitle=" + autoTitle +
                ", autoDetail=" + autoDetail +
                '}';
    }
}
