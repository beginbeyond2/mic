package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsLin implements ISerialsDetails {
    private RightBeanSelect source;
    private RightBeanSelect linType;
    private RightBeanSelect idleLevel;
    private RightBeanSelect baudRate;
    private RxStringWithSelect baudRateDefine;
    //baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

    public RightBeanSelect getLinType() {
        return linType;
    }

    public void setLinType(RightBeanSelect linType) {
        if (this.linType == null) {
            this.linType = linType;
        } else {
            this.linType = linType;
            setAllUnSelect();
            this.linType.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getSource() {
        return source;
    }

    public void setSource(RightBeanSelect source) {
        if (this.source == null) {
            this.source = source;
        } else {
            this.source = source;
            setAllUnSelect();
            this.source.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getIdleLevel() {
        return idleLevel;
    }

    public void setIdleLevel(RightBeanSelect idleLevel) {
        if (this.idleLevel == null) {
            this.idleLevel = idleLevel;
        } else {
            this.idleLevel = idleLevel;
            setAllUnSelect();
            this.idleLevel.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getBaudRate() {
        return baudRate;
    }

    public void setBaudRate(RightBeanSelect baudRate) {
        if (this.baudRate == null) {
            this.baudRate = baudRate;
        } else {
            this.baudRate = baudRate;
            setAllUnSelect();
            this.baudRate.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getBaudRateDefine() {
        return baudRateDefine;
    }

    public void setBaudRateDefine(String baudRateDefine) {
        if (this.baudRateDefine == null) {
            this.baudRateDefine = new RxStringWithSelect(baudRateDefine);
        } else {
            this.baudRateDefine.setValue(baudRateDefine);
            setAllUnSelect();
            this.baudRateDefine.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        source.setRxMsgSelect(false);
        linType.setRxMsgSelect(false);
        idleLevel.setRxMsgSelect(false);
        baudRate.setRxMsgSelect(false);
        baudRateDefine.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgSerialsLin{" +
                "source=" + source +
                ", linType=" + linType +
                ", idleLevel=" + idleLevel +
                ", baudRate=" + baudRate +
                '}';
    }
}
