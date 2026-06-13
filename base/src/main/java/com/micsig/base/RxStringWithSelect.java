package com.micsig.base;

/**
 * Created by yangj on 2017/5/22.
 */

public class RxStringWithSelect extends RxMsgSelect implements Cloneable {
    private String value;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public RxStringWithSelect(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "RxStringWithSelect{" +
                "value='" + value + '\'' +
                "rxMsgSelect='" + rxMsgSelect + '\'' +
                '}';
    }
}
