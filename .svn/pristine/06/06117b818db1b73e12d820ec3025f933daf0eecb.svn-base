package com.micsig.tbook.tbookscope.rightslipmenu;

import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxIntWithSelect;
import com.micsig.tbook.ui.bean.RxStringWithSelect;
import com.micsig.tbook.ui.rightslipmenu.RightBeanSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/5/12.
 */

public class RightMsgMath {
    public static final int MATHTYPE_DW = CacheUtil.MATHTYPE_DW;
    public static final int MATHTYPE_FFT = CacheUtil.MATHTYPE_FFT;
    public static final int MATHTYPE_AXB = CacheUtil.MATHTYPE_AXB;
    public static final int MATHTYPE_AM = CacheUtil.MATHTYPE_AM;

    private RxBooleanWithSelect mathCheck;
    private RxIntWithSelect mathType;
    private RightBeanSelect mathTypeSelect;
    private RightBeanSelect dwSource1;
    private RightBeanSelect dwSymbol;
    private RightBeanSelect dwSource2;
    private RxStringWithSelect fftType;
    private RightBeanSelect fftSource;
    private RightBeanSelect fftWindow;
    private RightBeanSelect fftPersist;
    private RxStringWithSelect axbUnit;
    private RightBeanSelect axbSource;
    private RxStringWithSelect axbA;
    private RxStringWithSelect axbB;
    private RxStringWithSelect amUnit;
    private RxStringWithSelect amFormula;
    private RxStringWithSelect amVar1Number;
    private RxStringWithSelect amVar1Power;
    private RxStringWithSelect amVar2Number;
    private RxStringWithSelect amVar2Power;
    private RxStringWithSelect label;


    private int mathChannelNumber = TChan.Math1;//默认Math1

    private boolean isUpClick;

    public boolean isUpClick() {
        return isUpClick;
    }

    public void setUpClick(boolean upClick) {
        isUpClick = upClick;
    }

    public int getMathChannelNumber() {
        return mathChannelNumber;
    }

    public void setMathChannelNumber(int mathChannelNumber) {
        this.mathChannelNumber = mathChannelNumber;
    }

    public RxBooleanWithSelect getMathCheck() {
        return mathCheck;
    }

    public void setMathCheck(boolean mathCheck) {
        if (this.mathCheck == null) {
            this.mathCheck = new RxBooleanWithSelect(mathCheck);
        } else {
            this.mathCheck.setValue(mathCheck);
            setAllUnSelect();
            this.mathCheck.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getMathType() {
        return mathType;
    }

    public void setMathType(int mathType) {
        if (this.mathType == null) {
            this.mathType = new RxIntWithSelect(mathType);
        } else {
            this.mathType.setValue(mathType);
            setAllUnSelect();
            this.mathType.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getMathTypeSelect() {
        return mathTypeSelect;
    }

    public void setMathTypeSelect(RightBeanSelect mathTypeSelect) {
        if (this.mathTypeSelect == null) {
            this.mathTypeSelect = mathTypeSelect;
        } else {
            this.mathTypeSelect = mathTypeSelect;
            setAllUnSelect();
            this.mathTypeSelect.setRxMsgSelect(true);
        }
    }


    public RightBeanSelect getDwSource1() {
        return dwSource1;
    }

    public void setDwSource1(RightBeanSelect dwSource1) {
        if (this.dwSource1 == null) {
            this.dwSource1 = dwSource1;
        } else {
            this.dwSource1 = dwSource1;
            setAllUnSelect();
            this.dwSource1.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getDwSymbol() {
        return dwSymbol;
    }

    public void setDwSymbol(RightBeanSelect dwSymbol) {
        if (this.dwSymbol == null) {
            this.dwSymbol = dwSymbol;
        } else {
            this.dwSymbol = dwSymbol;
            setAllUnSelect();
            this.dwSymbol.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getDwSource2() {
        return dwSource2;
    }

    public void setDwSource2(RightBeanSelect dwSource2) {
        if (this.dwSource2 == null) {
            this.dwSource2 = dwSource2;
        } else {
            this.dwSource2 = dwSource2;
            setAllUnSelect();
            this.dwSource2.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getFftType() {
        return fftType;
    }

    public void setFftType(String fftType) {
        if (this.fftType == null) {
            this.fftType = new RxStringWithSelect(fftType);
        } else {
            this.fftType.setValue(fftType);
            setAllUnSelect();
            this.mathType.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getFftSource() {
        return fftSource;
    }

    public void setFftSource(RightBeanSelect fftSource) {
        if (this.fftSource == null) {
            this.fftSource = fftSource;
        } else {
            this.fftSource = fftSource;
            setAllUnSelect();
            this.fftSource.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getFftWindow() {
        return fftWindow;
    }

    public void setFftWindow(RightBeanSelect fftWindow) {
        if (this.fftWindow == null) {
            this.fftWindow = fftWindow;
        } else {
            this.fftWindow = fftWindow;
            setAllUnSelect();
            this.fftWindow.setRxMsgSelect(true);
        }
    }
    public RightBeanSelect getFftPersist(){return fftPersist;}
    public void setFftPersist(RightBeanSelect fftPersist){
        if (this.fftPersist==null){
            this.fftPersist=fftPersist;
        }else {
            this.fftPersist=fftPersist;
            setAllUnSelect();
            this.fftPersist.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAxbUnit() {
        return axbUnit;
    }

    public void setAxbUnit(String axbUnit) {
        if (this.axbUnit == null) {
            this.axbUnit = new RxStringWithSelect(axbUnit);
        } else {
            this.axbUnit.setValue(axbUnit);
            setAllUnSelect();
            this.axbUnit.setRxMsgSelect(true);
        }
    }

    public RightBeanSelect getAxbSource() {
        return axbSource;
    }

    public void setAxbSource(RightBeanSelect axbSource) {
        if (this.axbSource == null) {
            this.axbSource = axbSource;
        } else {
            this.axbSource = axbSource;
            setAllUnSelect();
            this.axbSource.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAxbA() {
        return axbA;
    }

    public void setAxbA(String axbA) {
        if (this.axbA == null) {
            this.axbA = new RxStringWithSelect(axbA);
        } else {
            this.axbA.setValue(axbA);
            setAllUnSelect();
            this.axbA.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAxbB() {
        return axbB;
    }

    public void setAxbB(String axbB) {
        if (this.axbB == null) {
            this.axbB = new RxStringWithSelect(axbB);
        } else {
            this.axbB.setValue(axbB);
            setAllUnSelect();
            this.axbB.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmUnit() {
        return amUnit;
    }

    public void setAmUnit(String amUnit) {
        if (this.amUnit == null) {
            this.amUnit = new RxStringWithSelect(amUnit);
        } else {
            this.amUnit.setValue(amUnit);
            setAllUnSelect();
            this.amUnit.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmFormula() {
        return amFormula;
    }

    public void setAmFormula(String amFormula) {
        if (this.amFormula == null) {
            this.amFormula = new RxStringWithSelect(amFormula);
        } else {
            this.amFormula.setValue(amFormula);
            setAllUnSelect();
            this.amFormula.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmVar1Number() {
        return amVar1Number;
    }

    public void setAmVar1Number(String amVar1Number) {
        if (this.amVar1Number == null) {
            this.amVar1Number = new RxStringWithSelect(amVar1Number);
        } else {
            this.amVar1Number.setValue(amVar1Number);
            setAllUnSelect();
            this.amVar1Number.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmVar1Power() {
        return amVar1Power;
    }

    public void setAmVar1Power(String amVar1Power) {
        if (this.amVar1Power == null) {
            this.amVar1Power = new RxStringWithSelect(amVar1Power);
        } else {
            this.amVar1Power.setValue(amVar1Power);
            setAllUnSelect();
            this.amVar1Power.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmVar2Number() {
        return amVar2Number;
    }

    public void setAmVar2Number(String amVar2Number) {
        if (this.amVar2Number == null) {
            this.amVar2Number = new RxStringWithSelect(amVar2Number);
        } else {
            this.amVar2Number.setValue(amVar2Number);
            setAllUnSelect();
            this.amVar2Number.setRxMsgSelect(true);
        }
    }

    public RxStringWithSelect getAmVar2Power() {
        return amVar2Power;
    }

    public void setAmVar2Power(String amVar2Power) {
        if (this.amVar2Power == null) {
            this.amVar2Power = new RxStringWithSelect(amVar2Power);
        } else {
            this.amVar2Power.setValue(amVar2Power);
            setAllUnSelect();
            this.amVar2Power.setRxMsgSelect(true);
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

    private void setAllUnSelect() {
        mathType.setRxMsgSelect(false);
        dwSource1.setRxMsgSelect(false);
        dwSymbol.setRxMsgSelect(false);
        dwSource2.setRxMsgSelect(false);
        fftType.setRxMsgSelect(false);
        fftSource.setRxMsgSelect(false);
        fftWindow.setRxMsgSelect(false);
        fftPersist.setRxMsgSelect(false);
        axbUnit.setRxMsgSelect(false);
        axbSource.setRxMsgSelect(false);
        axbA.setRxMsgSelect(false);
        axbB.setRxMsgSelect(false);
        amUnit.setRxMsgSelect(false);
        amFormula.setRxMsgSelect(false);
        amVar1Number.setRxMsgSelect(false);
        amVar1Power.setRxMsgSelect(false);
        amVar2Number.setRxMsgSelect(false);
        amVar2Power.setRxMsgSelect(false);
        label.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "RightMsgMath{" +
                "mathCheck=" + mathCheck +
                ", mathType=" + mathType +
                ", dwSource1=" + dwSource1 +
                ", dwSymbol=" + dwSymbol +
                ", dwSource2=" + dwSource2 +
                ", fftType=" + fftType +
                ", fftSource=" + fftSource +
                ", fftWindow=" + fftWindow +
                ", axbUnit=" + axbUnit +
                ", axbSource=" + axbSource +
                ", axbA=" + axbA +
                ", axbB=" + axbB +
                ", amUnit=" + amUnit +
                ", amFormula=" + amFormula +
                ", amVar1Number=" + amVar1Number +
                ", amVar1Power=" + amVar1Power +
                ", amVar2Number=" + amVar2Number +
                ", amVar2Power=" + amVar2Power +
                ", label=" + label +
                '}';
    }
}
