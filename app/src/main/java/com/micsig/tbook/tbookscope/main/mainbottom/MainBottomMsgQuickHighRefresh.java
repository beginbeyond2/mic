package com.micsig.tbook.tbookscope.main.mainbottom;



/**
 * Created by yangj on 2017/8/21.
 */

public class MainBottomMsgQuickHighRefresh  {
    private boolean refresh;

    public MainBottomMsgQuickHighRefresh(boolean refresh) {
        this.refresh = refresh;
    }

    public boolean isRefresh() {
        return refresh;
    }

    public void setRefresh(boolean refresh) {
        this.refresh = refresh;
    }
}
