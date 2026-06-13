package com.micsig.tbook.tbookscope.main.mainright;

import com.micsig.tbook.tbookscope.util.CacheUtil;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.wavezone.TChan;

/**
 * Created by yangj on 2017/5/15.
 */

public class MainRightMsgOthers {
    private static final String TAG = MainRightMsgOthers.class.getSimpleName();

    private RxBooleanWithSelect math1, math2, math3, math4, math5, math6, math7, math8;
    private RxBooleanWithSelect ref1, ref2, ref3, ref4, ref5, ref6, ref7, ref8;
    private RxBooleanWithSelect s1, s2, s3, s4;

    private boolean s1Draw, s2Draw, s3Draw, s4Draw;

    public void setSerialsDraw(int serialNumber, boolean serialDraw) {
        switch (serialNumber) {
            case CacheUtil.S1:
                setS1Draw(serialDraw);
                break;
            case CacheUtil.S2:
                setS2Draw(serialDraw);
                break;
            case CacheUtil.S3:
                setS3Draw(serialDraw);
                break;
            case CacheUtil.S4:
                setS4Draw(serialDraw);
                break;
        }
    }

    public boolean getSerialsDraw(int serialNumber) {
        switch (serialNumber) {
            case CacheUtil.S1:
                return isS1Draw();
            case CacheUtil.S2:
                return isS2Draw();
            case CacheUtil.S3:
                return isS3Draw();
            case CacheUtil.S4:
                return isS4Draw();
            default:
                return isS1Draw();
        }
    }

    public RxBooleanWithSelect getSerial(int serialNumber) {
        switch (serialNumber) {
            case CacheUtil.S1:
                return getS1();
            case CacheUtil.S2:
                return getS2();
            case CacheUtil.S3:
                return getS3();
            case CacheUtil.S4:
                return getS4();
            default:
                return getS1();
        }
    }

    public void setSerials(int serialNumber, boolean serialCheck) {
        switch (serialNumber) {
            case CacheUtil.S1:
                setS1(serialCheck);
                break;
            case CacheUtil.S2:
                setS2(serialCheck);
                break;
            case CacheUtil.S3:
                setS3(serialCheck);
                break;
            case CacheUtil.S4:
                setS4(serialCheck);
                break;
            default:
                setS1(serialCheck);
                break;
        }
    }

    public RxBooleanWithSelect getRef(int refChannel) {
        switch (refChannel) {
            case TChan.R1:
                return getRef1();
            case TChan.R2:
                return getRef2();
            case TChan.R3:
                return getRef3();
            case TChan.R4:
                return getRef4();
            case TChan.R5:
                return getRef5();
            case TChan.R6:
                return getRef6();
            case TChan.R7:
                return getRef7();
            case TChan.R8:
                return getRef8();
            default:
                return getRef1();
        }
    }

    public void setRef(int recChannel, boolean refCheck) {
        switch (recChannel) {
            case TChan.R1:
                setRef1(refCheck);
                break;
            case TChan.R2:
                setRef2(refCheck);
                break;
            case TChan.R3:
                setRef3(refCheck);
                break;
            case TChan.R4:
                setRef4(refCheck);
                break;
            case TChan.R5:
                setRef5(refCheck);
                break;
            case TChan.R6:
                setRef6(refCheck);
                break;
            case TChan.R7:
                setRef7(refCheck);
                break;
            case TChan.R8:
                setRef8(refCheck);
                break;
        }
    }

    public RxBooleanWithSelect getMath(int mathChannel) {
        switch (mathChannel) {
            case TChan.Math1:
                return getMath1();
            case TChan.Math2:
                return getMath2();
            case TChan.Math3:
                return getMath3();
            case TChan.Math4:
                return getMath4();
            case TChan.Math5:
                return getMath5();
            case TChan.Math6:
                return getMath6();
            case TChan.Math7:
                return getMath7();
            case TChan.Math8:
                return getMath8();
            default:
                return getMath1();
        }
    }

    public void setMath(int mathChannel, boolean mathCheck) {
        switch (mathChannel) {
            case TChan.Math1:
                setMath1(mathCheck);
                break;
            case TChan.Math2:
                setMath2(mathCheck);
                break;
            case TChan.Math3:
                setMath3(mathCheck);
                break;
            case TChan.Math4:
                setMath4(mathCheck);
                break;
            case TChan.Math5:
                setMath5(mathCheck);
                break;
            case TChan.Math6:
                setMath6(mathCheck);
                break;
            case TChan.Math7:
                setMath7(mathCheck);
                break;
            case TChan.Math8:
                setMath8(mathCheck);
                break;
            default:
                setMath1(mathCheck);
                break;
        }
    }

    public RxBooleanWithSelect getMath1() {
        return math1;
    }

    public void setMath1(boolean math) {
        if (this.math1 == null) {
            this.math1 = new RxBooleanWithSelect(math);
        } else {
            this.math1.setValue(math);
            setAllUnSelect();
            this.math1.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath2() {
        return math2;
    }

    public void setMath2(boolean math) {
        if (this.math2 == null) {
            this.math2 = new RxBooleanWithSelect(math);
        } else {
            this.math2.setValue(math);
            setAllUnSelect();
            this.math2.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath3() {
        return math3;
    }

    public void setMath3(boolean math) {
        if (this.math3 == null) {
            this.math3 = new RxBooleanWithSelect(math);
        } else {
            this.math3.setValue(math);
            setAllUnSelect();
            this.math3.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath4() {
        return math4;
    }

    public void setMath4(boolean math) {
        if (this.math4 == null) {
            this.math4 = new RxBooleanWithSelect(math);
        } else {
            this.math4.setValue(math);
            setAllUnSelect();
            this.math4.setRxMsgSelect(true);
        }
    }


    public RxBooleanWithSelect getMath5() {
        return math5;
    }

    public void setMath5(boolean math) {
        if (this.math5 == null) {
            this.math5 = new RxBooleanWithSelect(math);
        } else {
            this.math5.setValue(math);
            setAllUnSelect();
            this.math5.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath6() {
        return math6;
    }

    public void setMath6(boolean math) {
        if (this.math6 == null) {
            this.math6 = new RxBooleanWithSelect(math);
        } else {
            this.math6.setValue(math);
            setAllUnSelect();
            this.math6.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath7() {
        return math7;
    }

    public void setMath7(boolean math) {
        if (this.math7 == null) {
            this.math7 = new RxBooleanWithSelect(math);
        } else {
            this.math7.setValue(math);
            setAllUnSelect();
            this.math7.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getMath8() {
        return math8;
    }

    public void setMath8(boolean math) {
        if (this.math8 == null) {
            this.math8 = new RxBooleanWithSelect(math);
        } else {
            this.math8.setValue(math);
            setAllUnSelect();
            this.math8.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getS1() {
        return s1;
    }

    public void setS1(boolean s1) {
        if (this.s1 == null) {
            this.s1 = new RxBooleanWithSelect(s1);
        } else {
            this.s1.setValue(s1);
            setAllUnSelect();
            this.s1.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getS2() {
        return s2;
    }

    public void setS2(boolean s2) {
        if (this.s2 == null) {
            this.s2 = new RxBooleanWithSelect(s2);
        } else {
            this.s2.setValue(s2);
            setAllUnSelect();
            this.s2.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getS3() {
        return s3;
    }

    public void setS3(boolean s3) {
        if (this.s3 == null) {
            this.s3 = new RxBooleanWithSelect(s3);
        } else {
            this.s3.setValue(s3);
            setAllUnSelect();
            this.s3.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getS4() {
        return s4;
    }

    public void setS4(boolean s4) {
        if (this.s4 == null) {
            this.s4 = new RxBooleanWithSelect(s4);
        } else {
            this.s4.setValue(s4);
            setAllUnSelect();
            this.s4.setRxMsgSelect(true);
        }
    }


    public RxBooleanWithSelect getRef1() {
        return ref1;
    }

    public void setRef1(boolean ref1) {
        if (this.ref1 == null) {
            this.ref1 = new RxBooleanWithSelect(ref1);
        } else {
            this.ref1.setValue(ref1);
            setAllUnSelect();
            this.ref1.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef2() {
        return ref2;
    }

    public void setRef2(boolean ref2) {
        if (this.ref2 == null) {
            this.ref2 = new RxBooleanWithSelect(ref2);
        } else {
            this.ref2.setValue(ref2);
            setAllUnSelect();
            this.ref2.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef3() {
        return ref3;
    }

    public void setRef3(boolean ref3) {
        if (this.ref3 == null) {
            this.ref3 = new RxBooleanWithSelect(ref3);
        } else {
            this.ref3.setValue(ref3);
            setAllUnSelect();
            this.ref3.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef4() {
        return ref4;
    }

    public void setRef4(boolean ref4) {
        if (this.ref4 == null) {
            this.ref4 = new RxBooleanWithSelect(ref4);
        } else {
            this.ref4.setValue(ref4);
            setAllUnSelect();
            this.ref4.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef5() {
        return ref5;
    }

    public void setRef5(boolean ref5) {
        if (this.ref5 == null) {
            this.ref5 = new RxBooleanWithSelect(ref5);
        } else {
            this.ref5.setValue(ref5);
            setAllUnSelect();
            this.ref5.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef6() {
        return ref6;
    }

    public void setRef6(boolean ref6) {
        if (this.ref6 == null) {
            this.ref6 = new RxBooleanWithSelect(ref6);
        } else {
            this.ref6.setValue(ref6);
            setAllUnSelect();
            this.ref6.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef7() {
        return ref7;
    }

    public void setRef7(boolean ref7) {
        if (this.ref7 == null) {
            this.ref7 = new RxBooleanWithSelect(ref7);
        } else {
            this.ref7.setValue(ref7);
            setAllUnSelect();
            this.ref7.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getRef8() {
        return ref8;
    }

    public void setRef8(boolean ref8) {
        if (this.ref8 == null) {
            this.ref8 = new RxBooleanWithSelect(ref8);
        } else {
            this.ref8.setValue(ref8);
            setAllUnSelect();
            this.ref8.setRxMsgSelect(true);
        }
    }


    public boolean isS1Draw() {
        return s1Draw;
    }

    public void setS1Draw(boolean s1Draw) {
        this.s1Draw = s1Draw;
    }

    public boolean isS2Draw() {
        return s2Draw;
    }

    public void setS2Draw(boolean s2Draw) {
        this.s2Draw = s2Draw;
    }

    public boolean isS3Draw() {
        return s3Draw;
    }

    public void setS3Draw(boolean s3Draw) {
        this.s3Draw = s3Draw;
    }

    public boolean isS4Draw() {
        return s4Draw;
    }

    public void setS4Draw(boolean s4Draw) {
        this.s4Draw = s4Draw;
    }

    public void setAllUnSelect() {
        math1.setRxMsgSelect(false);
        math2.setRxMsgSelect(false);
        math3.setRxMsgSelect(false);
        math4.setRxMsgSelect(false);
        math5.setRxMsgSelect(false);
        math6.setRxMsgSelect(false);
        math7.setRxMsgSelect(false);
        math8.setRxMsgSelect(false);
        ref1.setRxMsgSelect(false);
        ref2.setRxMsgSelect(false);
        ref3.setRxMsgSelect(false);
        ref4.setRxMsgSelect(false);
        ref5.setRxMsgSelect(false);
        ref6.setRxMsgSelect(false);
        ref7.setRxMsgSelect(false);
        ref8.setRxMsgSelect(false);
        s1.setRxMsgSelect(false);
        s2.setRxMsgSelect(false);
        s3.setRxMsgSelect(false);
        s4.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("MainRightMsgOthers{");
        sb.append("math1=").append(math1).append(", ");
        sb.append("math2=").append(math2).append(", ");
        sb.append("math3=").append(math3).append(", ");
        sb.append("math4=").append(math4).append(", ");
        sb.append("math5=").append(math5).append(", ");
        sb.append("math6=").append(math6).append(", ");
        sb.append("math7=").append(math7).append(", ");
        sb.append("math8=").append(math8).append(", ");
        sb.append("ref1=").append(ref1).append(", ");
        sb.append("ref2=").append(ref2).append(", ");
        sb.append("ref3=").append(ref3).append(", ");
        sb.append("ref4=").append(ref4).append(", ");
        sb.append("ref5=").append(ref5).append(", ");
        sb.append("ref6=").append(ref6).append(", ");
        sb.append("ref7=").append(ref7).append(", ");
        sb.append("ref8=").append(ref8).append(", ");
        sb.append("s1=").append(s1).append(", ");
        sb.append("s2=").append(s2).append(", ");
        sb.append("s3=").append(s3).append(", ");
        sb.append("s4=").append(s4);
        sb.append("}");
        return sb.toString();
    }
}
