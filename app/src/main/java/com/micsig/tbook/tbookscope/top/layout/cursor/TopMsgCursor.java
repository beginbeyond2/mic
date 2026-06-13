package com.micsig.tbook.tbookscope.top.layout.cursor;

import com.micsig.tbook.tbookscope.top.layout.measure.IMeasureDetail;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgCursor {
    private TopAllBeanTitle cursorTitle;
    private IMeasureDetail cursorDetail;
    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getCursorTitle() {
        return cursorTitle;
    }

    public void setCursorTitle(TopAllBeanTitle cursorTitle) {
        if (this.cursorTitle == null) {
            this.cursorTitle = cursorTitle;
        } else {
            this.cursorTitle = cursorTitle;
            this.cursorTitle.setRxMsgSelect(true);
        }
    }

    public IMeasureDetail getCursorDetail() {
        return cursorDetail;
    }

    public void setCursorDetail(IMeasureDetail cursorDetail) {
        this.cursorDetail = cursorDetail;
    }

    @Override
    public String toString() {
        return "TopMsgMeasure{" +
                "cursorTitle=" + cursorTitle +
                ", cursorDetail=" + cursorDetail +
                '}';
    }
}
