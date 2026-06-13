package com.micsig.tbook.tbookscope.main.maincenter;

import com.micsig.tbook.scope.channel.SegmentedSingleBean;
import com.micsig.tbook.ui.bean.RxBooleanWithSelect;
import com.micsig.tbook.ui.bean.RxIntWithSelect;


public class MainMsgCenterSegmented implements Cloneable {
    private RxBooleanWithSelect display;//单帧为true，拟合为false...
    private RxBooleanWithSelect playing;//是否正在播放...
    private RxIntWithSelect playSpeed;
    private RxIntWithSelect count;
    private RxBooleanWithSelect singleOrder;
    private RxBooleanWithSelect singleLarge;
    private SegmentedSingleBean curSingleFrame;
    private SegmentedSingleBean fitStart;
    private SegmentedSingleBean fitEnd;

    public RxBooleanWithSelect isDisplay() {
        return display;
    }

    public void setDisplay(boolean display) {
        if (this.display == null) {
            this.display = new RxBooleanWithSelect(display);
        } else {
            this.display.setValue(display);
            setAllUnSelect();
            this.display.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect isPlaying() {
        return playing;
    }

    public void setPlaying(boolean playing) {
        if (this.playing == null) {
            this.playing = new RxBooleanWithSelect(playing);
        } else {
            this.playing.setValue(playing);
            setAllUnSelect();
            this.playing.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getPlaySpeed() {
        return playSpeed;
    }

    public void setPlaySpeed(int playSpeed) {
        if (this.playSpeed == null) {
            this.playSpeed = new RxIntWithSelect(playSpeed);
        } else {
            this.playSpeed.setValue(playSpeed);
            setAllUnSelect();
            this.playSpeed.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getCount() {
        return count;
    }

    public void setCount(int count) {
        if (this.count == null) {
            this.count = new RxIntWithSelect(count);
        } else {
            this.count.setValue(count);
            setAllUnSelect();
            this.count.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect isSingleOrder() {
        return singleOrder;
    }

    public void setSingleOrder(boolean singleOrder) {
        if (this.singleOrder == null) {
            this.singleOrder = new RxBooleanWithSelect(singleOrder);
        } else {
            this.singleOrder.setValue(singleOrder);
            setAllUnSelect();
            this.singleOrder.setRxMsgSelect(true);
        }
    }

    public RxBooleanWithSelect isSingleLarge() {
        return singleLarge;
    }

    public void setSingleLarge(boolean singleLarge) {
        if (this.singleLarge == null) {
            this.singleLarge = new RxBooleanWithSelect(singleLarge);
        } else {
            this.singleLarge.setValue(singleLarge);
            setAllUnSelect();
            this.singleLarge.setRxMsgSelect(true);
        }
    }

    public SegmentedSingleBean getCurSingleFrame() {
        return curSingleFrame;
    }

    public void setCurSingleFrame(SegmentedSingleBean curSingleFrame) {
        if (this.curSingleFrame == null) {
            this.curSingleFrame = curSingleFrame;
        } else {
            this.curSingleFrame = curSingleFrame;
            setAllUnSelect();
            this.curSingleFrame.setRxMsgSelect(true);
        }
    }

    public SegmentedSingleBean getFitStart() {
        return fitStart;
    }

    public void setFitStart(SegmentedSingleBean fitStart) {
        if (this.fitStart == null) {
            this.fitStart = fitStart;
        } else {
            this.fitStart = fitStart;
            setAllUnSelect();
            this.fitStart.setRxMsgSelect(true);
        }
    }

    public SegmentedSingleBean getFitEnd() {
        return fitEnd;
    }

    public void setFitEnd(SegmentedSingleBean fitEnd) {
        if (this.fitEnd == null) {
            this.fitEnd = fitEnd;
        } else {
            this.fitEnd = fitEnd;
            setAllUnSelect();
            this.fitEnd.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        display.setRxMsgSelect(false);
        playing.setRxMsgSelect(false);
        playSpeed.setRxMsgSelect(false);
        count.setRxMsgSelect(false);
        singleOrder.setRxMsgSelect(false);
        singleLarge.setRxMsgSelect(false);
        if (curSingleFrame!= null) {
            curSingleFrame.setRxMsgSelect(false);
        }
        if (fitStart != null) {
            fitStart.setRxMsgSelect(false);
        }
        if (fitEnd != null) {
            fitEnd.setRxMsgSelect(false);
        }
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "MainMsgCenterSegmented{" +
                "display=" + display +
                ", playing=" + playing +
                ", playSpeed=" + playSpeed +
                ", count=" + count +
                ", singleOrder=" + singleOrder +
                ", singleLarge=" + singleLarge +
                ", curSingleFrame=" + curSingleFrame +
                ", fitStart=" + fitStart +
                ", fitEnd=" + fitEnd +
                '}';
    }
}
