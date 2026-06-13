package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerialsCan implements ISerialsDetails {
    private RightBeanSelect source;
    private RightBeanSelect signal;
    private RightBeanSelect baudRate;
    private RxStringWithSelect baudRateDefine;
    private RightBeanSelect fdBaudRate;
    private RxStringWithSelect fdBaudRateDefine;
    private RightBeanSelect iSO;
    //baudRateDefine不为""，则用baudRateDefine的值，为""，则用baudRate的值...

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

    public RightBeanSelect getSignal() {
        return signal;
    }

    public void setSignal(RightBeanSelect signal) {
        if (this.signal == null) {
            this.signal = signal;
        } else {
            this.signal = signal;
            setAllUnSelect();
            this.signal.setRxMsgSelect(true);
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

    public RightBeanSelect getFDBaudRate() {
        return fdBaudRate;
    }

    public void setFDBaudRate(RightBeanSelect fdBaudRate) {
        if (this.fdBaudRate == null) {
            this.fdBaudRate = fdBaudRate;
        } else {
            this.fdBaudRate = fdBaudRate;
            setAllUnSelect();
            this.fdBaudRate.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getFDBaudRateDefine() {
        return fdBaudRateDefine;
    }

    public void setFDBaudRateDefine(String fdBaudRateDefine) {
        if (this.fdBaudRateDefine == null) {
            this.fdBaudRateDefine = new RxStringWithSelect(fdBaudRateDefine);
        } else {
            this.fdBaudRateDefine.setValue(fdBaudRateDefine);
            setAllUnSelect();
            this.fdBaudRateDefine.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getISO() {
        return iSO;
    }

    public void setISO(RightBeanSelect iSO) {
        if (this.iSO == null) {
            this.iSO = iSO;
        } else {
            this.iSO = iSO;
            setAllUnSelect();
            this.iSO.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        this.source.setRxMsgSelect(false);
        this.signal.setRxMsgSelect(false);
        this.baudRate.setRxMsgSelect(false);
        this.fdBaudRate.setRxMsgSelect(false);
        this.iSO.setRxMsgSelect(false);
        baudRateDefine.setRxMsgSelect(false);
        fdBaudRateDefine.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgSerialsCan{" +
                "source=" + source +
                ", signal=" + signal +
                ", baudRate=" + baudRate +
                ", baudRateDefine=" + baudRateDefine +
                ", fdBaudRate=" + fdBaudRate +
                ", fdBaudRateDefine=" + fdBaudRateDefine +
                ", iSO=" + iSO +
                '}';
    }
}
