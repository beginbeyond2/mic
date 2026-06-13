package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailCanRemoteId implements ISerialsDetail {
    private DataBean canRemoteIdEdit;
    private String canRemoteIdEditTitle;

    public String getCanRemoteIdEditTitle() {
        return canRemoteIdEditTitle;
    }

    public void setCanRemoteIdEditTitle(String canRemoteIdEditTitle) {
        this.canRemoteIdEditTitle = canRemoteIdEditTitle;
    }

    public DataBean getCanRemoteIdEdit() {
        return canRemoteIdEdit;
    }

    public void setCanRemoteIdEdit(int digits, String value) {
        if (this.canRemoteIdEdit == null) {
            this.canRemoteIdEdit = new DataBean();
        }
        this.canRemoteIdEdit.setDigits(digits);
        this.canRemoteIdEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailCanRemoteId{" +
                "canRemoteIdEdit='" + canRemoteIdEdit + '\'' +
                '}';
    }
}
