package com.micsig.tbook.tbookscope.top.layout.save;
import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgSaveStore {
    private TopAllBeanTitle saveTitle;
    private ISaveDetail saveDetail;
    private boolean isFromEventBus;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public TopAllBeanTitle getSaveTitle() {
        return saveTitle;
    }

    public void setSaveTitle(TopAllBeanTitle saveTitle) {
        if (this.saveTitle == null) {
            this.saveTitle = saveTitle;
        } else {
            this.saveTitle = saveTitle;
            this.saveTitle.setRxMsgSelect(true);
        }
    }

    public ISaveDetail getSaveDetail() {
        return saveDetail;
    }

    public void setSaveDetail(ISaveDetail saveDetail) {
        this.saveDetail = saveDetail;
    }

    @Override
    public String toString() {
        return "TopMsgSaveStore{" +
                "saveTitle=" + saveTitle +
                ", saveDetail=" + saveDetail +
                '}';
    }
}
