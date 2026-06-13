package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailCanDataId implements ISerialsDetail {
    private DataBean canDataIdEdit;
    private String canDataIdEditTitle;

    public String getCanDataIdEditTitle() {
        return canDataIdEditTitle;
    }

    public void setCanDataIdEditTitle(String canDataIdEditTitle) {
        this.canDataIdEditTitle = canDataIdEditTitle;
    }

    public DataBean getCanDataIdEdit() {
        return canDataIdEdit;
    }

    public void setCanDataIdEdit(int digits, String value) {
        if (this.canDataIdEdit == null) {
            this.canDataIdEdit = new DataBean();
        }
        this.canDataIdEdit.setDigits(digits);
        this.canDataIdEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailCanDataId{" +
                "canDataIdEdit='" + canDataIdEdit + '\'' +
                '}';
    }
}
