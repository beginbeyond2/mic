package com.micsig.tbook.tbookscope.top.layout.userset;

import com.micsig.tbook.ui.top.view.title.TopAllBeanTitle;

/**
 * Created by yangj on 2017/5/17.
 */

public class TopMsgUserset {
    private TopAllBeanTitle usersetTitle;

    public TopAllBeanTitle getUsersetTitle() {
        return usersetTitle;
    }

    public void setUsersetTitle(TopAllBeanTitle usersetTitle) {
        if (this.usersetTitle == null) {
            this.usersetTitle = usersetTitle;
        } else {
            this.usersetTitle = usersetTitle;
            this.usersetTitle.setRxMsgSelect(true);
        }
    }

    @Override
    public String toString() {
        return "TopMsgUserset{" +
                "usersetTitle=" + usersetTitle +
                '}';
    }
}
