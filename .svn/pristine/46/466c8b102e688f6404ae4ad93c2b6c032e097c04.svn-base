package com.micsig.tbook.tbookscope.top.layout.display;

import com.micsig.tbook.ui.bean.RxIntWithSelect;
import com.micsig.tbook.ui.top.view.channel.TopBeanChannel;

/**
 * Created by yangj on 2017/5/16.
 */

public class TopMsgDisplayWaveform implements IDisplayDetail {
    private TopBeanChannel drawType;
    private TopBeanChannel background;
    private RxIntWithSelect brightness;

    public TopBeanChannel getBackground(){return background;}

    public void setBackground(TopBeanChannel background){
        if (this.background == null) {
            this.background = background;
        } else {
            this.background = background;
            setAllUnSelect();
            this.background.setRxMsgSelect(true);
        }
    }
    public TopBeanChannel getDrawType() {
        return drawType;
    }

    public void setDrawType(TopBeanChannel drawType) {
        if (this.drawType == null) {
            this.drawType = drawType;
        } else {
            this.drawType = drawType;
            setAllUnSelect();
            this.drawType.setRxMsgSelect(true);
        }
    }

    public RxIntWithSelect getBrightness() {
        return brightness;
    }

    public void setBrightness(int brightness) {
        if (this.brightness == null) {
            this.brightness = new RxIntWithSelect(brightness);
        } else {
            this.brightness.setValue(brightness);
            setAllUnSelect();
            this.brightness.setRxMsgSelect(true);
        }
    }

    private void setAllUnSelect() {
        drawType.setRxMsgSelect(false);
        brightness.setRxMsgSelect(false);
        background.setRxMsgSelect(false);
    }

    @Override
    public String toString() {
        return "TopMsgDisplayWaveform{" +
                "drawType=" + drawType +
                ", background=" + background +
                ", brightness=" + brightness +
                '}';
    }
}
