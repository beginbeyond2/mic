package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;

/**
 * Created by yangj on 2017/5/12.
 */

public class RightMsgChannel {

    /**
     * value 1-8
     */
    private int channelNumber;
    private RxBooleanWithSelect chCheck;
    private RxBooleanWithSelect invert;
    private RightBeanSelect couple;
    private RightBeanSelect probeType;
    private RxStringWithSelect probeMultiple;
    private RightBeanSelect bandWidth;
    private RxStringWithSelect bandWidthEdit;
    private RightBeanSelect imped;
    private RxStringWithSelect label;
    private RxStringWithSelect delay;

    private RxStringWithSelect fineExtent;

    private RxBooleanWithSelect fineSwitch;
    private double probeMulScale;//改变比，改变前的值除以改变后的值

    private boolean isFromEventBus;
    private boolean isUpClick;

    public boolean isUpClick() {
        return isUpClick;
    }

    public void setUpClick(boolean upClick) {
        isUpClick = upClick;
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public RxBooleanWithSelect getChCheck() {
        return chCheck;
    }

    public void setChCheck(boolean chCheck) {
        if (this.chCheck == null) {
            this.chCheck = new RxBooleanWithSelect(chCheck);
        } else {
            this.chCheck.setValue(chCheck);
            setAllUnSelect();
            this.chCheck.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getInvert() {
        return invert;
    }

    public void setInvert(boolean invert) {
        if (this.invert == null) {
            this.invert = new RxBooleanWithSelect(invert);
        } else {
            this.invert.setValue(invert);
            setAllUnSelect();
            this.invert.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getCouple() {
        return couple;
    }

    public void setCouple(RightBeanSelect couple) {
        if (this.couple == null) {
            this.couple = couple;
        } else {
            this.couple = couple;
            setAllUnSelect();
            this.couple.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getProbeType() {
        return probeType;
    }

    public void setProbeType(RightBeanSelect probeType) {
        if (this.probeType == null) {
            this.probeType = probeType;
        } else {
            this.probeType = probeType;
            setAllUnSelect();
            this.probeType.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getProbeMultiple() {
        return probeMultiple;
    }

    public void setProbeMultiple(String probeMultiple) {
        if (this.probeMultiple == null) {
            this.probeMultiple = new RxStringWithSelect(probeMultiple);
        } else {
            this.probeMultiple.setValue(probeMultiple);
            setAllUnSelect();
            this.probeMultiple.setRxMsgSelect(true);
        }
    }

    public double getProbeMulScale() {
        return probeMulScale;
    }

    public void setProbeMulScale(double probeMulScale) {
        this.probeMulScale = probeMulScale;
    }

    public RightBeanSelect getBandWidth() {
        return bandWidth;
    }

    public void setBandWidth(RightBeanSelect bandWidth) {
        if (this.bandWidth == null) {
            this.bandWidth = bandWidth;
        } else {
            this.bandWidth = bandWidth;
            setAllUnSelect();
            this.bandWidth.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getBandWidthEdit() {
        return bandWidthEdit;
    }

    public void setBandWidthEdit(String bandWidthEdit) {
        if (this.bandWidthEdit == null) {
            this.bandWidthEdit = new RxStringWithSelect(bandWidthEdit);
        } else {
            this.bandWidthEdit.setValue(bandWidthEdit);
            setAllUnSelect();
            this.bandWidthEdit.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getImped() {
        return imped;
    }

    public void setImped(RightBeanSelect Imped) {
        if (this.imped == null) {
            this.imped = Imped;
        } else {
            this.imped = Imped;
            setAllUnSelect();
            this.imped.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getLabel() {
        return label;
    }

    public void setLabel(String label) {
        if (this.label == null) {
            this.label = new RxStringWithSelect(label);
        } else {
            this.label.setValue(label);
            setAllUnSelect();
            this.label.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        if (this.delay == null) {
            this.delay = new RxStringWithSelect(delay);
        } else {
            this.delay.setValue(delay);
            setAllUnSelect();
            this.delay.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getFineExtent(){return fineExtent;}
    public void setFineExtent(String fineExtent){
        if (this.fineExtent==null){
            this.fineExtent=new RxStringWithSelect(fineExtent);
        }else {
            this.fineExtent.setValue(fineExtent);
            setAllUnSelect();
            this.fineExtent.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getFineSwitch(){return fineSwitch;}
    public void setFineSwitch(boolean fineSwitch){
        if (this.fineSwitch==null){
            this.fineSwitch=new RxBooleanWithSelect(fineSwitch);
        }else {
            this.fineSwitch.setValue(fineSwitch);
            setAllUnSelect();
            this.fineSwitch.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        invert.setRxMsgSelect(false);
        couple.setRxMsgSelect(false);
        probeType.setRxMsgSelect(false);
        probeMultiple.setRxMsgSelect(false);
        bandWidth.setRxMsgSelect(false);
        bandWidthEdit.setRxMsgSelect(false);
        imped.setRxMsgSelect(false);
        label.setRxMsgSelect(false);
        delay.setRxMsgSelect(false);
        fineExtent.setRxMsgSelect(false);
        fineSwitch.setRxMsgSelect(false);

    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RightMsgChannel{");
        sb.append("channelNumber=").append(channelNumber);
        sb.append(", chCheck=").append(chCheck);
        sb.append(", invert=").append(invert);
        sb.append(", couple=").append(couple);
        sb.append(", probeType=").append(probeType);
        sb.append(", probeMultiple=").append(probeMultiple);
        sb.append(", bandWidth=").append(bandWidth);
        sb.append(", bandWidthEdit=").append(bandWidthEdit);
        sb.append(", imped=").append(imped);
        sb.append(", label=").append(label);
        sb.append(", delay=").append(delay);
        sb.append(", fineExtent=").append(fineExtent);
        sb.append(", fineSwitch=").append(fineSwitch);
        sb.append(", probeMulScale=").append(probeMulScale);
        sb.append(", isFromEventBus=").append(isFromEventBus);
        sb.append(", isUpClick=").append(isUpClick);
        sb.append('}');
        return sb.toString();
    }
}
