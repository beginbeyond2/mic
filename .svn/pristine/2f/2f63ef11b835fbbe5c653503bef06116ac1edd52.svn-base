package com.micsig.tbook.tbookscope.top.layout.sample;

import com.micsig.tbook.ui.bean.RxIntWithSelect;

/**
 * Created by yangj on 2017/5/27.
 */

public class TopMsgSampleDepth {
    private RxIntWithSelect depth;
    private boolean isFromEventBus;

    public TopMsgSampleDepth(boolean isFromEventBus) {
        this.isFromEventBus = isFromEventBus;
    }

    public RxIntWithSelect getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        if (this.depth == null) {
            this.depth = new RxIntWithSelect(depth);
        } else {
            this.depth.setValue(depth);
        }
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }
}
