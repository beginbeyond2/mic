package com.micsig.base;

/**
 * Created by yangj on 2017/5/22.
 */

public class RxBooleanWithSelect extends RxMsgSelect {
    private boolean value;

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public RxBooleanWithSelect(boolean value) {
        this.value = value;
    }

    /**
     * 既是当前选择，又是选择的打开
     */
    public boolean isCurSelectTrue() {
        return value && rxMsgSelect;
    }

    @Override
    public String toString() {
        return "RxBooleanWithSelect{" +
                "value=" + value +
                "rxMsgSelect=" + rxMsgSelect +
                '}';
    }
}
