package com.micsig.tbook.tbookscope.rightslipmenu.serials;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

import java.io.Serializable;

/**
 * Created by yangj on 2017/5/15.
 */

public class RightMsgSerials implements Serializable {
    public static final int SERIALS_S1 = 1;
    public static final int SERIALS_S2 = 2;
    public static final int SERIALS_S3 = 3;
    public static final int SERIALS_S4 = 4;

    private RxBooleanWithSelect serials1Check, serials2Check, serials3Check, serials4Check;
    private int serialsNumber;
    private RightBeanSelect serialsType;
    private ISerialsDetails serialsDetails;
    private boolean openLevel = true;//本消息是否打开电平页面,并设置
    private boolean isFromEventBus = false;

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public RxBooleanWithSelect getSerialsCheck(int serialsNumber) {
        switch (serialsNumber) {
            case 2:
                return getSerials2Check();
            case 3:
                return getSerials3Check();
            case 4:
                return getSerials4Check();
            case 1:
            default:
                return getSerials1Check();
        }
    }

    public RxBooleanWithSelect getSerials1Check() {
        return serials1Check;
    }

    public RxBooleanWithSelect getSerials2Check() {
        return serials2Check;
    }

    public RxBooleanWithSelect getSerials3Check() {
        return serials3Check;
    }

    public RxBooleanWithSelect getSerials4Check() {
        return serials4Check;
    }


    public void setSerialsCheck(int serialsNumber, boolean state) {
        switch (serialsNumber) {
            case 2:
                setSerials2Check(state);
                break;
            case 3:
                setSerials3Check(state);
                break;
            case 4:
                setSerials4Check(state);
                break;
            case 1:
            default:
                setSerials1Check(state);
                break;
        }
    }

    public void setSerials1Check(boolean serials1Check) {
        if (this.serials1Check == null) {
            this.serials1Check = new RxBooleanWithSelect(serials1Check);
        } else {
            this.serials1Check.setValue(serials1Check);
            this.serials1Check.setRxMsgSelect(true);
        }
    }

    public void setSerials2Check(boolean serials2Check) {
        if (this.serials2Check == null) {
            this.serials2Check = new RxBooleanWithSelect(serials2Check);
        } else {
            this.serials2Check.setValue(serials2Check);
            this.serials2Check.setRxMsgSelect(true);
        }
    }

    public void setSerials3Check(boolean serials3Check) {
        if (this.serials3Check == null) {
            this.serials3Check = new RxBooleanWithSelect(serials3Check);
        } else {
            this.serials3Check.setValue(serials3Check);
            this.serials3Check.setRxMsgSelect(true);
        }
    }

    public void setSerials4Check(boolean serials4Check) {
        if (this.serials4Check == null) {
            this.serials4Check = new RxBooleanWithSelect(serials4Check);
        } else {
            this.serials4Check.setValue(serials4Check);
            this.serials4Check.setRxMsgSelect(true);
        }
    }

    public boolean isOpenLevel() {
        return openLevel;
    }

    public void setOpenLevel(boolean openLevel) {
        this.openLevel = openLevel;
    }

    public int getSerialsNumber() {
        return serialsNumber;
    }

    public void setSerialsNumber(int serialsNumber) {
        this.serialsNumber = serialsNumber;
    }

    public RightBeanSelect getSerialsType() {
        return serialsType;
    }

    public void setSerialsType(RightBeanSelect serialsType) {
        if (this.serialsType == null) {
            this.serialsType = serialsType;
        } else {
            this.serialsType = serialsType;
            this.serialsType.setRxMsgSelect(true);
        }
    }

    public ISerialsDetails getSerialsDetails() {
        return serialsDetails;
    }

    public void setSerialsDetails(ISerialsDetails serialsDetails) {
        this.serialsDetails = serialsDetails;
    }

    public boolean isSerials1() {
        return serialsNumber == SERIALS_S1;
    }

    public boolean isSerials2() {
        return serialsNumber == SERIALS_S2;
    }

    public boolean isSerials3() {
        return serialsNumber == SERIALS_S3;
    }

    public boolean isSerials4() {
        return serialsNumber == SERIALS_S4;
    }

    @Override
    public String toString() {
        return "RightMsgSerials{" +
                "serialsNumber=" + serialsNumber +
                ", serialsType=" + serialsType +
                ", serialsDetails=" + serialsDetails +
                '}';
    }
}
