package com.micsig.tbook.tbookscope.main.mainright;

import com.micsig.base.DoubleUtil;
import com.micsig.tbook.scope.channel.ChannelFactory;
import com.micsig.tbook.tbookscope.GlobalVar;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.wavezone.TChan;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by yangj on 2017/5/15.
 */

public class MainRightMsgChannels {
    private RxBooleanWithSelect ch1;
    private RxBooleanWithSelect ch2;
    private RxBooleanWithSelect ch3;
    private RxBooleanWithSelect ch4;
    private RxBooleanWithSelect ch5;
    private RxBooleanWithSelect ch6;
    private RxBooleanWithSelect ch7;
    private RxBooleanWithSelect ch8;

    /**
     * 单位的变化比例，值为改变之前的数字除以改变之的数字
     */
    private double ch1Scale;
    private double ch2Scale;
    private double ch3Scale;
    private double ch4Scale;
    private double ch5Scale;
    private double ch6Scale;
    private double ch7Scale;
    private double ch8Scale;

    private final double[] chxScale = {ch1Scale, ch2Scale, ch3Scale, ch4Scale, ch5Scale, ch6Scale, ch7Scale, ch8Scale};

    private boolean isFromEventBus = false;

    private int channelCount = GlobalVar.get().getChannelsCount();

    private void setAllChannelsScaleChangeFalse() {
        ch1Scale = 0;
        ch2Scale = 0;
        ch3Scale = 0;
        ch4Scale = 0;
        ch5Scale = 0;
        ch6Scale = 0;
        ch7Scale = 0;
        ch8Scale = 0;
//        Arrays.fill(chxScale, 0);
    }

    /**
     * 是否是channel开关状态发生变化。不是通道开关状态发生的变化，则本次是通道档位发生的变化的信号。
     */
    public boolean isChangeChState() {
        return !(isCh1ScaleChange() || isCh2ScaleChange()
                || isCh3ScaleChange() || isCh4ScaleChange()
                || isCh5ScaleChange() || isCh6ScaleChange()
                || isCh7ScaleChange() || isCh8ScaleChange()
        );
    }

    public boolean isChangeChScaleState(int chIdx) {
        switch (chIdx) {
            case ChannelFactory.CH1:
                return isCh1ScaleChange();
            case ChannelFactory.CH2:
                return isCh2ScaleChange();
            case ChannelFactory.CH3:
                return isCh3ScaleChange();
            case ChannelFactory.CH4:
                return isCh4ScaleChange();
            case ChannelFactory.CH5:
                return isCh5ScaleChange();
            case ChannelFactory.CH6:
                return isCh6ScaleChange();
            case ChannelFactory.CH7:
                return isCh7ScaleChange();
            case ChannelFactory.CH8:
                return isCh8ScaleChange();
        }
        return false;
    }

    public void setCh(int chIdx, boolean select) {
        switch (chIdx) {
            case ChannelFactory.CH1:
                setCh1(select);
                break;
            case ChannelFactory.CH2:
                setCh2(select);
                break;
            case ChannelFactory.CH3:
                setCh3(select);
                break;
            case ChannelFactory.CH4:
                setCh4(select);
                break;
            case ChannelFactory.CH5:
                setCh5(select);
                break;
            case ChannelFactory.CH6:
                setCh6(select);
                break;
            case ChannelFactory.CH7:
                setCh7(select);
                break;
            case ChannelFactory.CH8:
                setCh8(select);
                break;
        }
    }

    public RxBooleanWithSelect getCh(int chIdx) {
        switch (chIdx) {
            case ChannelFactory.CH1:
                return getCh1();
            case ChannelFactory.CH2:
                return getCh2();
            case ChannelFactory.CH3:
                return getCh3();
            case ChannelFactory.CH4:
                return getCh4();
            case ChannelFactory.CH5:
                return getCh5();
            case ChannelFactory.CH6:
                return getCh6();
            case ChannelFactory.CH7:
                return getCh7();
            case ChannelFactory.CH8:
                return getCh8();
            default:
                return getCh1();
        }
    }

    public void setChScale(int chIdx, double scale) {
        switch (chIdx) {
            case ChannelFactory.CH1:
                setCh1Scale(scale);
                break;
            case ChannelFactory.CH2:
                setCh2Scale(scale);
                break;
            case ChannelFactory.CH3:
                setCh3Scale(scale);
                break;
            case ChannelFactory.CH4:
                setCh4Scale(scale);
                break;
            case ChannelFactory.CH5:
                setCh5Scale(scale);
                break;
            case ChannelFactory.CH6:
                setCh6Scale(scale);
                break;
            case ChannelFactory.CH7:
                setCh7Scale(scale);
                break;
            case ChannelFactory.CH8:
                setCh8Scale(scale);
                break;
        }
    }

    public double getChScale(int chIdx) {
        switch (chIdx) {
            case ChannelFactory.CH1:
                return getCh1Scale();
            case ChannelFactory.CH2:
                return getCh2Scale();
            case ChannelFactory.CH3:
                return getCh3Scale();
            case ChannelFactory.CH4:
                return getCh4Scale();
            case ChannelFactory.CH5:
                return getCh5Scale();
            case ChannelFactory.CH6:
                return getCh6Scale();
            case ChannelFactory.CH7:
                return getCh7Scale();
            case ChannelFactory.CH8:
                return getCh8Scale();
            default:
                return getCh1Scale();
        }
    }

    public boolean isFromEventBus() {
        return isFromEventBus;
    }

    public void setFromEventBus(boolean fromEventBus) {
        isFromEventBus = fromEventBus;
    }

    public double getCh1Scale() {
        return ch1Scale;
    }

    public void setCh1Scale(double ch1Scale) {
        this.ch1Scale = ch1Scale;
    }

    public double getCh2Scale() {
        return ch2Scale;
    }

    public void setCh2Scale(double ch2Scale) {
        this.ch2Scale = ch2Scale;
    }

    public double getCh3Scale() {
        return ch3Scale;
    }

    public void setCh3Scale(double ch3Scale) {
        this.ch3Scale = ch3Scale;
    }

    public double getCh4Scale() {
        return ch4Scale;
    }

    public void setCh4Scale(double ch4Scale) {
        this.ch4Scale = ch4Scale;
    }

    public double getCh5Scale() {
        return ch5Scale;
    }

    public void setCh5Scale(double ch5Scale) {
        this.ch5Scale = ch5Scale;
    }

    public double getCh6Scale() {
        return ch6Scale;
    }

    public void setCh6Scale(double ch6Scale) {
        this.ch6Scale = ch6Scale;
    }

    public double getCh7Scale() {
        return ch7Scale;
    }

    public void setCh7Scale(double ch7Scale) {
        this.ch7Scale = ch7Scale;
    }


    public double getCh8Scale() {
        return ch8Scale;
    }

    public void setCh8Scale(double ch8Scale) {
        this.ch8Scale = ch8Scale;
    }

    public boolean isCh1ScaleChange() {
        return DoubleUtil.compareTo(ch1Scale, 0.0) != 0;
    }

    public boolean isCh2ScaleChange() {
        return DoubleUtil.compareTo(ch2Scale, 0.0) != 0;
    }

    public boolean isCh3ScaleChange() {
        return DoubleUtil.compareTo(ch3Scale, 0.0) != 0;
    }

    public boolean isCh4ScaleChange() {
        return DoubleUtil.compareTo(ch4Scale, 0.0) != 0;
    }

    public boolean isCh5ScaleChange() {
        return DoubleUtil.compareTo(ch5Scale, 0.0) != 0;
    }

    public boolean isCh6ScaleChange() {
        return DoubleUtil.compareTo(ch6Scale, 0.0) != 0;
    }

    public boolean isCh7ScaleChange() {
        return DoubleUtil.compareTo(ch7Scale, 0.0) != 0;
    }

    public boolean isCh8ScaleChange() {
        return DoubleUtil.compareTo(ch8Scale, 0.0) != 0;
    }

    public void setCh1(boolean ch1) {
        setAllChannelsScaleChangeFalse();
        if (this.ch1 == null) {
            this.ch1 = new RxBooleanWithSelect(ch1);
        } else {
            this.ch1.setValue(ch1);
            setAllUnSelect();
            this.ch1.setRxMsgSelect(true);
        }
    }

    public void setCh2(boolean ch2) {
        setAllChannelsScaleChangeFalse();
        if (this.ch2 == null) {
            this.ch2 = new RxBooleanWithSelect(ch2);
        } else {
            this.ch2.setValue(ch2);
            setAllUnSelect();
            this.ch2.setRxMsgSelect(true);

        }
    }

    public void setCh3(boolean ch3) {
        setAllChannelsScaleChangeFalse();
        if (this.ch3 == null) {
            this.ch3 = new RxBooleanWithSelect(ch3);
        } else {
            this.ch3.setValue(ch3);
            setAllUnSelect();
            this.ch3.setRxMsgSelect(true);
        }
    }

    public void setCh4(boolean ch4) {
        setAllChannelsScaleChangeFalse();
        if (this.ch4 == null) {
            this.ch4 = new RxBooleanWithSelect(ch4);
        } else {
            this.ch4.setValue(ch4);
            setAllUnSelect();
            this.ch4.setRxMsgSelect(true);
        }
    }
    public void setCh5(boolean ch5) {
        setAllChannelsScaleChangeFalse();
        if (this.ch5 == null) {
            this.ch5 = new RxBooleanWithSelect(ch5);
        } else {
            this.ch5.setValue(ch5);
            setAllUnSelect();
            this.ch5.setRxMsgSelect(true);
        }
    }
    public void setCh6(boolean ch6) {
        setAllChannelsScaleChangeFalse();
        if (this.ch6 == null) {
            this.ch6 = new RxBooleanWithSelect(ch6);
        } else {
            this.ch6.setValue(ch6);
            setAllUnSelect();
            this.ch6.setRxMsgSelect(true);
        }
    }
    public void setCh7(boolean ch7) {
        setAllChannelsScaleChangeFalse();
        if (this.ch7 == null) {
            this.ch7= new RxBooleanWithSelect(ch7);
        } else {
            this.ch7.setValue(ch7);
            setAllUnSelect();
            this.ch7.setRxMsgSelect(true);
        }
    }
    public void setCh8(boolean ch8) {
        setAllChannelsScaleChangeFalse();
        if (this.ch8 == null) {
            this.ch8 = new RxBooleanWithSelect(ch8);
        } else {
            this.ch8.setValue(ch8);
            setAllUnSelect();
            this.ch8.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect getCh1() {
        return ch1;
    }
    public RxBooleanWithSelect getCh2() {
        return ch2;
    }
    public RxBooleanWithSelect getCh3() {
        return ch3;
    }
    public RxBooleanWithSelect getCh4() {return ch4;}
    public RxBooleanWithSelect getCh5() {
        return ch5;
    }
    public RxBooleanWithSelect getCh6(){
        return ch6;
    }
    public RxBooleanWithSelect getCh7(){
        return ch7;
    }
    public RxBooleanWithSelect getCh8(){
        return ch8;
    }
    private void setAllUnSelect() {
        if (ch1!=null) ch1.setRxMsgSelect(false);
        if (ch2!=null) ch2.setRxMsgSelect(false);
        if (channelCount == GlobalVar.CHANNEL_COUNT_4) {
            if (ch3!=null) ch3.setRxMsgSelect(false);
            if (ch4!=null) ch4.setRxMsgSelect(false);
        } else if (channelCount == GlobalVar.CHANNEL_COUNT_8) {
            if (ch3!=null) ch3.setRxMsgSelect(false);
            if (ch4!=null) ch4.setRxMsgSelect(false);
            if (ch5!=null) ch5.setRxMsgSelect(false);
            if (ch6!=null) ch6.setRxMsgSelect(false);
            if (ch7!=null) ch7.setRxMsgSelect(false);
            if (ch8!=null) ch8.setRxMsgSelect(false);
        }
    }

    public void setAllChScale(double scale) {
        setCh1Scale(scale);
        setCh2Scale(scale);
        setCh3Scale(scale);
        setCh4Scale(scale);
        setCh5Scale(scale);
        setCh6Scale(scale);
        setCh7Scale(scale);
        setCh8Scale(scale);
    }

    /**
     * 检测其中之一是否有变化
     */
    public boolean isChangeChXState() {
        return !(isChXScaleChange(0) || isChXScaleChange(1)
                || isChXScaleChange(2) || isChXScaleChange(3)
                || isChXScaleChange(4) || isChXScaleChange(5)
                || isChXScaleChange(6) || isChXScaleChange(7));
    }

    //某个通道scale是否变化
    public boolean isChXScaleChange(int chIndex) {
        if (chIndex >= chxScale.length || chIndex < 0) return false;
        return DoubleUtil.compareTo(chxScale[chIndex], 0.0) != 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("MainRightMsgChannels{");
        sb.append("ch1=").append(ch1);
        sb.append(", ch2=").append(ch2);
        sb.append(", ch3=").append(ch3);
        sb.append(", ch4=").append(ch4);
        sb.append(", ch5=").append(ch5);
        sb.append(", ch6=").append(ch6);
        sb.append(", ch7=").append(ch7);
        sb.append(", ch8=").append(ch8);
        sb.append(", ch1Scale=").append(ch1Scale);
        sb.append(", ch2Scale=").append(ch2Scale);
        sb.append(", ch3Scale=").append(ch3Scale);
        sb.append(", ch4Scale=").append(ch4Scale);
        sb.append(", ch5Scale=").append(ch5Scale);
        sb.append(", ch6Scale=").append(ch6Scale);
        sb.append(", ch7Scale=").append(ch7Scale);
        sb.append(", ch8Scale=").append(ch8Scale);
        sb.append(", isFromEventBus=").append(isFromEventBus);
        sb.append('}');
        return sb.toString();

    }
}
