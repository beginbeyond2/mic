package com.micsig.tbook.tbookscope.top.layout.trigger.serials.bean;

/**
 * Created by yangj on 2017/5/17.
 */

public class SerialsDetailCanRdId implements ISerialsDetail {
    private DataBean canRdIdEdit;
    private String canRdIdEditTitle;

    public String getCanRdIdEditTitle() {
        return canRdIdEditTitle;
    }

    public void setCanRdIdEditTitle(String canRdIdEditTitle) {
        this.canRdIdEditTitle = canRdIdEditTitle;
    }

    public DataBean getCanRdIdEdit() {
        return canRdIdEdit;
    }

    public void setCanRdIdEdit(int digits, String value) {
        if (this.canRdIdEdit == null) {
            this.canRdIdEdit = new DataBean();
        }
        this.canRdIdEdit.setDigits(digits);
        this.canRdIdEdit.setValue(value);
    }

    @Override
    public String toString() {
        return "SerialsDetailCanRdId{" +
                "canRdIdEdit='" + canRdIdEdit + '\'' +
                '}';
    }
}
