package com.micsig.tbook.ui.bean;

import java.io.Serializable;

/**
 * Created by yangj on 2017/5/22.
 */

public class RxMsgSelect implements Serializable {
    protected boolean rxMsgSelect = false;

    public boolean isRxMsgSelect() {
        return rxMsgSelect;
    }

    public void setRxMsgSelect(boolean rxMsgSelect) {
        this.rxMsgSelect = rxMsgSelect;
    }
}
